/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vanuston.medeil.dao;

import com.vanuston.medeil.implementation.Sales;
import com.vanuston.medeil.model.CreditNoteModel;
import com.vanuston.medeil.model.SalesModel;
import com.vanuston.medeil.model.StockModel;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import com.vanuston.medeil.util.DBConnection;
import com.vanuston.medeil.util.DateUtils;
import com.vanuston.medeil.util.Logger;
import java.util.ArrayList;
import com.vanuston.medeil.util.Value;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 *
 * @author Administrator
 */
public class SalesDAO implements Sales {
    static Logger log=Logger.getLogger(SalesDAO.class,"com.vanuston.medeil.dao.SalesDAO");
    public int getCntFnRow;

    //Temporary Stock table insertion
    @Override
    public boolean insertTempStock(SalesModel sales) {
        boolean insertFlag = false;
        SalesModel tempSales = sales;
        try {

            DBConnection.getStatement().executeUpdate("INSERT INTO temp_stocks (item_code,item_name,batch_no,qty,packing,expiry_date,mrp,cdate,temp_flag_id) values('" + tempSales.getItemCode() + "','" + tempSales.getItemName() + "','" + tempSales.getBatchNumber() + "','" + tempSales.getQuantity() + "','" + tempSales.getPacking() + "','" + tempSales.getExpiryDate() + "','" + tempSales.getMrp() + "',curdate(),'0')");
            insertFlag =true;
        }
        catch(Exception ex) {
           
            String msg = "Class :SalesDAO  Method : insertTempStock Exception : " + ex.getMessage();
            log.debug(msg);
        }
        return insertFlag;
    }

    //Stock subtraction
    @Override
    public boolean stockSub(SalesModel model)
    {
       ResultSet rs=null;
       int oldStockQty=0;
       int newStockQty=0;
       int oldSoldQty=0;
       int adjid=0;
       String itemName = "";
       Boolean updateFlag=false;
        try {
            if(!model.getUpdateFlag().equals("true")) {
                rs = DBConnection.getStatement().executeQuery("select item_name,item_code,batch_no,qty,total_items from sales_" + model.getTableName() + " where bill_no='" + model.getBillNumber() + "'");
            }
            else{
                rs = DBConnection.getStatement().executeQuery("select item_name,item_code,batch_no,qty,total_items,adj_id from sales_return where sales_return_no='" + model.getSalesReturnNumber() + "'");
            }
            while(rs.next()) {
                model.setOldItemCode(rs.getString("item_code"));
                model.setOldQty(rs.getInt("qty"));
                model.setOldBatch(rs.getString("batch_no"));
                itemName = rs.getString("item_name");
                adjid=rs.getInt("adj_id");
                if(adjid==0) {
                    StockDAO stock=new StockDAO();
                    String stkQty = stock.getStockQty(model.getOldItemCode(),model.getOldBatch());
                    if(stkQty!=null && !stkQty.equals("null") && !stkQty.trim().equals("")) {
                        oldStockQty=Integer.parseInt(stock.getStockQty(model.getOldItemCode(),model.getOldBatch()));
                    }
                    oldSoldQty=model.getOldQty();
                    newStockQty=oldStockQty-oldSoldQty;
                    DBConnection.getStatement().executeUpdate("update stock_statement set qty='" + newStockQty + "' where item_code='"+model.getOldItemCode()+"' and batch_no='"+model.getOldBatch()+"'");
                    String sql1 = "insert into stock_register values(now(),'"+model.getOldItemCode()+"','"+itemName+"','"+model.getOldBatch()+"','"+oldStockQty+"','"+oldSoldQty+"','"+newStockQty+"','Sales Maintenance (-)','"+model.getBillNumber()+"')";
                    DBConnection.getStatement().executeUpdate(sql1);
                }
                else {
                    int adjQty=0;
                    int physcialQty=0;
                    ResultSet rs1=DBConnection.getStatement().executeQuery("select adjusted_stock as qty,physical_stock from stock_adjustment where id='"+adjid+"'");
                    while(rs1.next()) {
                        adjQty=rs1.getInt("qty");
                        physcialQty=rs1.getInt("physical_stock");
                        newStockQty=adjQty+model.getOldQty();
                        physcialQty=physcialQty+model.getOldQty();
                        DBConnection.getStatement().executeUpdate("update stock_adjustment set adjusted_stock='" + newStockQty + "',physical_stock='"+physcialQty+"' where id='"+adjid+"'");
                    }
                    
                }                
            }
            updateFlag=true;
        }
        catch (Exception ex) {
            String msg = "Class :SalesDAO  Method : stockSub Exception : " + ex.getMessage();
            log.debug(msg);
        }
      return updateFlag;
    }


    //Stock Addition while updating in Sales Maintenance
    //Method purpose : While updating in sales maintenance, the rows in sales bills are deleted. So stock is added back
    @Override
    public boolean stockAdd(SalesModel model)
    {
        ResultSet rs=null;
        int oldStockQty=0;
        int newStockQty=0;
        int oldSoldQty=0;
        int retQty=0;
        int adjid=0;
        String itemName = "";
        Boolean updateFlag=false;
        try
        {
            if(!model.getBillType().equals("counter"))
            {
              rs=DBConnection.getStatement().executeQuery("select s.item_name as item_name,s.item_code as item_code,s.batch_no as batch_no,s.qty as qty,s.total_items as total_items,s.adj_id,"
                      + "coalesce(sr.qty,0) as retqty from sales_"+model.getBillType()+"_bill"+" s left join sales_return sr on s.bill_no=sr.bill_no and s.batch_no=sr.batch_no and s.item_code=sr.item_code "
                      + "and s.item_name=sr.item_name where s.bill_no='"+model.getBillNumber()+"'");
            }
            else
            {
              rs=DBConnection.getStatement().executeQuery("select s.item_name as item_name,s.item_code as item_code,s.batch_no as batch_no,s.qty as qty,s.total_items as total_items,0 as adj_id,"
                      + "coalesce(sr.qty,0) as retqty from sales_accounts s left join sales_return sr on s.bill_no=sr.bill_no and s.batch_no=sr.batch_no and s.item_code=sr.item_code and "
                      + "s.item_name=sr.item_name where s.bill_no='"+model.getBillNumber()+"'");
            }
            while(rs.next())
            {
                model.setOldItemCode(rs.getString("item_code"));
                model.setOldQty(rs.getInt("qty"));
                model.setOldBatch(rs.getString("batch_no"));
                itemName = rs.getString("item_name");
                retQty = rs.getInt("retqty");
                adjid=rs.getInt("adj_id");
                if(adjid==0) {
                    StockDAO stock=new StockDAO();
                    String stkQty = stock.getStockQty(model.getOldItemCode(),model.getOldBatch());
                    if(stkQty!=null && !stkQty.equals("null") && !stkQty.trim().equals("")) {
                        oldStockQty=Integer.parseInt(stock.getStockQty(model.getOldItemCode(),model.getOldBatch()));
                    }
                    oldSoldQty=model.getOldQty();
                    newStockQty=oldStockQty+oldSoldQty-retQty;
                    DBConnection.getStatement().executeUpdate("update stock_statement set qty='" + newStockQty + "',ss_flag_id = 0 where item_code='"+model.getOldItemCode()+"' and batch_no='"+model.getOldBatch()+"'");
                    if(!model.getFormType().equals("cancelbill")) {
                        String sql1 = "insert into stock_register values(now(),'"+model.getOldItemCode()+"','"+itemName+"','"+model.getOldBatch()+"','"+oldStockQty+"','"+(oldSoldQty-retQty)+"','"+newStockQty+"','Sales Maintenance (+)','"+model.getBillNumber()+"')";
                        DBConnection.getStatement().executeUpdate(sql1);
                    }
                }
                else {
                    int adjQty=0;
                    int physcialQty=0;
                    ResultSet rs1=DBConnection.getStatement().executeQuery("select adjusted_stock as qty,physical_stock from stock_adjustment where id='"+adjid+"'");
                    while(rs1.next()) {
                        adjQty=rs1.getInt("qty");
                        physcialQty=rs1.getInt("physical_stock");
                        newStockQty=adjQty-model.getOldQty();
                        physcialQty=physcialQty-model.getOldQty();
                        DBConnection.getStatement().executeUpdate("update stock_adjustment set adjusted_stock='" + newStockQty + "',physical_stock='"+physcialQty+"' where id='"+adjid+"'");
                    }                    
                }                
            }
            updateFlag=true;
        }
        catch(Exception ex) {           
            String msg = "Class :SalesDAO  Method : stockAdd()  Exception: " + ex.getMessage();
            log.debug(msg);
        }
        return updateFlag;
    }
    
    @Override
    public Object viewAllRecords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Save sales bill, sales return.
    //Sales cash,card or credit bill tables are inserted first. Then sales maintenance table is inserted.
    @Override
    public Object createRecord(Object object) {       
        Boolean insert = false;
        int returnFlagCount = 0;
        SalesModel salesModel = (SalesModel) object;
        List<SalesModel> list = salesModel.getListofitems();
        try
        {
        // Sales cash, credit, cards Insert
        for (int index = 0; index < list.size(); index++) {
            SalesModel iterateModel = list.get(index);
            CallableStatement salesCall=DBConnection.getConnection().prepareCall("{call pro_savesales( ?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            salesCall.setString(1,salesModel.getBillNumber());
            salesCall.setString(2,salesModel.getBillDate());
            salesCall.setString(3,salesModel.getCustomerName());
            salesCall.setString(4,salesModel.getDoctorName());
            salesCall.setString(5,iterateModel.getItemCode());
            salesCall.setString(6,iterateModel.getItemName());
            salesCall.setString(7,iterateModel.getFormulation());
            salesCall.setString(8,iterateModel.getBatchNumber());
            salesCall.setInt(9,iterateModel.getQuantity());
            salesCall.setDouble(10, iterateModel.getUnitPrice());
            salesCall.setString(11,iterateModel.getExpiryDate());
            salesCall.setDouble(12, iterateModel.getMrp());
            salesCall.setDouble(13, iterateModel.getUnitDiscount());
            salesCall.setDouble(14, iterateModel.getUnitVAT());
            salesCall.setDouble(15, iterateModel.getSubTotal());
            salesCall.setDouble(16, salesModel.getTotalDiscount());
            salesCall.setDouble(17, salesModel.getTotalVAT());
            salesCall.setDouble(18, salesModel.getTotalAmount());
            salesCall.setDouble(19, salesModel.getPaidAmount());
            salesCall.setDouble(20, salesModel.getBalanceAmount());
            salesCall.setInt(21, salesModel.getTotalItems());
            salesCall.setInt(22, salesModel.getTotalQuantity());
            salesCall.setString(23, salesModel.getPaymentMode());
            salesCall.setString(24, salesModel.getCardNumber());
            salesCall.setString(25, salesModel.getCardHolderName());
            salesCall.setString(26,salesModel.getBankName());
            salesCall.setString(27, salesModel.getCardExpiry());
            salesCall.setString(28, salesModel.getAccountNumber());
            salesCall.setString(29, salesModel.getBillType());
            salesCall.setString(30, salesModel.getFormType());
            salesCall.setString(31,salesModel.getSalesReturnNumber());
            salesCall.setString(32,salesModel.getSalesReturnDate());
            salesCall.setString(33,salesModel.getEmployeeID());
            //Adjustment ID is set in Total Items
            salesCall.setInt(34,iterateModel.getTotalItems());
            salesCall.setInt(35,0);
            salesCall.registerOutParameter(36,Types.INTEGER);
            salesCall.executeUpdate();
            int returnFlag=salesCall.getInt("flag");
            if(returnFlag==1){
               returnFlagCount++;
            }
            }
            // Maintenance Table Insert
             if(returnFlagCount==list.size() && !salesModel.getBillType().equals("dummy") && !salesModel.getFormType().equals("return") && !salesModel.getFormType().equals("cancelbill")){
             String paymentMode="";
             if(salesModel.getBillType().equals("cards")) {
                paymentMode = salesModel.getPaymentMode();
             }
             for (int index = 0; index <= 1; index++) {
                 CallableStatement maintenanceCall=DBConnection.getConnection().prepareCall("{call pro_savesales( ?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
                 maintenanceCall.setString(1, salesModel.getBillNumber());
                 maintenanceCall.setString(2, salesModel.getBillDate());
                 maintenanceCall.setString(3, salesModel.getCustomerName());
                 maintenanceCall.setString(4, salesModel.getDoctorName());
                 maintenanceCall.setString(5,"");
                 maintenanceCall.setString(6,"");
                 maintenanceCall.setString(7,"");
                 maintenanceCall.setString(8,"");
                 maintenanceCall.setInt(9,0);
                 maintenanceCall.setDouble(10, 0.00);
                 maintenanceCall.setString(11,"0000-00-00");
                 maintenanceCall.setDouble(12, 0.00);
                 maintenanceCall.setDouble(13, 0.00);
                 maintenanceCall.setDouble(14, 0.00);
                 maintenanceCall.setDouble(15, 0.00);
                 maintenanceCall.setDouble(16, 0.00);
                 maintenanceCall.setDouble(17, 0.00);
                 maintenanceCall.setDouble(18, salesModel.getTotalAmount());
                 maintenanceCall.setDouble(19, salesModel.getPaidAmount());
                 maintenanceCall.setDouble(20, salesModel.getBalanceAmount());
                 maintenanceCall.setInt(21, salesModel.getTotalItems());
                 maintenanceCall.setInt(22, salesModel.getTotalQuantity());
                 maintenanceCall.setString(23, paymentMode);
                 maintenanceCall.setString(24, salesModel.getCardNumber());
                 maintenanceCall.setString(25, salesModel.getCardHolderName());
                 maintenanceCall.setString(26, salesModel.getBankName());
                 maintenanceCall.setString(27, salesModel.getCardExpiry());
                 maintenanceCall.setString(28, salesModel.getAccountNumber());
                 if (index == 0 && !salesModel.getBillType().equals("counter")) {
                    maintenanceCall.setString(29, "savemaintenance");
                 } else if(index == 0 && salesModel.getBillType().equals("counter")) {
                    maintenanceCall.setString(29, "savecountermaintenance");
                 } else if (index == 1 && salesModel.getBillType().equals("cards")) {
                    maintenanceCall.setString(29, "bankbook");
                 } else {
                     maintenanceCall.setString(29, "");
                 }
                 maintenanceCall.setString(30,salesModel.getFormType());
                 maintenanceCall.setString(31,salesModel.getSalesReturnNumber());
                 maintenanceCall.setString(32,salesModel.getSalesReturnDate());
                 maintenanceCall.setString(33,salesModel.getEmployeeID());
                 maintenanceCall.setInt(34,0);
                 maintenanceCall.setInt(35, salesModel.getPrescriptionDays());
                 maintenanceCall.registerOutParameter(36,Types.INTEGER);
                 maintenanceCall.executeUpdate();
             }
            } else {
            // Rollback Sales items
            }
            insert = true;        
        }
        catch(Exception e)
        {           
           insert = false;
           log.debug(" Class : SalesDAO  Method   : CreateRecord Exception :" + e.getMessage()); 
        }
        System.out.println("Sales:"+insert);
        return insert;
    }

    //Values for Expiry Stock Alert dialog box in Sales
    @Override
    public Integer getAlertStatus() {
        int i = 0;
        try {
            ResultSet rs = null;
            String sql = "";
            sql = "SELECT expiry_alerts   FROM alert_setting";
            rs = DBConnection.getStatement().executeQuery(sql);
            while (rs.next()) {
                i = rs.getInt("expiry_alerts");
            }
        } catch (Exception ex) {
            String msg = "Class :SalesDAO  Method : getAlertStatus Exception : " + ex.getMessage();
            log.debug(msg);
        }

        return i;
    }

    
    @Override
    public int getTotTempQty(int rowIndex,int n,List<SalesModel> salesTableItems) {
        int totTmpQty = 0;
        try {            
            SalesModel model = salesTableItems.get(rowIndex);
            String itemName = model.getItemName();
            Integer quantity = model.getQuantity();
            String batchNo = model.getBatchNumber();

            for (int index = 0; index < salesTableItems.size(); index++) {
                SalesModel salesmodel = salesTableItems.get(index);
                String batch = salesmodel.getBatchNumber();               
                String tableItemName = salesmodel.getItemName();
               

                if(!tableItemName.equals("") && !batch.equals("")) {
                    if (itemName.trim().equals(tableItemName.trim()) && batch.trim().equals(batchNo.trim())) {
                            int rQty = salesmodel.getQuantity();
                            totTmpQty += rQty;
                            getCntFnRow = index;
                    }
                }
            }
            if(n==0) {
                totTmpQty -= quantity;
            }

        } catch (Exception ex) {
           
            String msg = "Class :SalesDAO  Method : getTotTempQty Exception : " + ex.getMessage();
            log.debug(msg);
        }
        return totTmpQty;
    }

    @Override
    public Object viewRecord(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Sales maintenance update - while clicking on update, the datas are actually deleted and inserted
    //Purpose: to delete values in sales cash,card or credit as well as maintenance tables
    @Override
    public boolean deleteRecord(Object object) {
        Boolean deleteFlag=false;
        SalesModel model = (SalesModel) object;
        try {
            if(!model.getFormType().equals("return")) {
            if(!model.getBillType().equals("counter")) {
            DBConnection.getStatement().executeUpdate("delete from sales_"+model.getBillType()+"_bill"+" where bill_no = '"+model.getBillNumber()+"'");
            }
            else {
            DBConnection.getStatement().executeUpdate("delete from sales_accounts where bill_no = '"+model.getBillNumber()+"'");
            }
            DBConnection.getStatement().executeUpdate("delete from sales_maintain_bill where bill_no='"+model.getBillNumber()+"'");
            deleteFlag=true;
            }
            else {
            DBConnection.getStatement().executeUpdate("delete from sales_return where sales_return_no = '"+model.getSalesReturnNumber()+"'");
            deleteFlag=true;
            }
        } catch (Exception ex) {          
           deleteFlag=false;
           String msg = "Class :SalesDAO  Method : deleteRecord  Exception: " + ex.getMessage();
           log.debug(msg);
        }
        return deleteFlag;
    }

    @Override
    public Object updateRecord(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object salesTableValidation(List<SalesModel> list, String billType,String formType,String billNumber, String tableName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //To get the sales quantity in sales table before making any changes in sales maintenance
    @Override
    public String getSoldQty(String billNumber,String itemCode, String batchNumber,String billType) {
        String getSoldQty="0";
        ResultSet rs=null;
        try {            
            if(!billType.equals("counter"))
            {
            rs=DBConnection.getStatement().executeQuery("select qty from sales_"+billType+"_bill"+" where bill_no='"+billNumber+"' and item_code='"+itemCode+"' and batch_no='"+batchNumber+"'");
            }
            else
            {
            rs=DBConnection.getStatement().executeQuery("select qty from sales_accounts where bill_no='"+billNumber+"' and item_code='"+itemCode+"' and batch_no='"+batchNumber+"'");
            }

            
            while(rs.next())
            {
                 if (rs.getString("qty") != null && !rs.getString("qty").equals("")) {
                    getSoldQty = rs.getString("qty").toString();
                }
            }
        }
        catch(Exception ex){          
           String msg = "Class :SalesDAO  Method : getSoldQty Exception: " + ex.getMessage();
           log.debug(msg);
        }
        return getSoldQty;
    }    



    @Override
    public Integer getSoldQty(String billNumber,String itemCode, String batchNumber,String billType, String tableName)
    {
        int soldQty=0;
        ResultSet rs=null;
        try
        {
        rs=DBConnection.getStatement().executeQuery("select qty from sales_"+tableName+" where bill_no='"+billNumber+"' and item_code='"+itemCode+"' and batch_no='"+batchNumber+"'");        
        while(rs.next())
        {
            soldQty=rs.getInt("qty");
        }
        }
        catch(Exception e)
        {
            String ss = " Class : SalesDAO  Method   : getSoldQty Exception :" + e.getMessage();
            log.debug(ss);
        }
        return soldQty;
    }


    //To insert in cedit note table in case of sales return
    @Override
    public CreditNoteModel insertCreditNoteNo(SalesModel sales) {
        ResultSet rs=null;
        CommonDAO commonDAO = new CommonDAO();
        CreditNoteModel creditModel=new CreditNoteModel();
        String creditNoteNo=commonDAO.getAutoIncrement(DateUtils.now("dd-MM-yyyy"),"CreditNotes");        
            try {
                if(!sales.getUpdateFlag().equals("true")){
                creditModel.setCreditNoteNumber(creditNoteNo);
                DBConnection.getStatement().executeUpdate("insert into credit_note (credit_note_no,issued_against,credit_opt,credit_date, invoiceorbill_no,name,amount,details,cre_flag_id) values('"+creditNoteNo+"','Customer','Sales Return','"+sales.getSalesReturnDate()+"','"+sales.getBillNumber()+"','"+sales.getCustomerName()+"',"+sales.getTotalAmount()+",'',0)");
                
                }
                else{
                rs=DBConnection.getStatement().executeQuery("select credit_note_no,details from credit_note where invoiceorbill_no = '"+sales.getBillNumber()+"'");
                while(rs.next())
                {
                   creditModel.setCreditNoteNumber(rs.getString("credit_note_no"));
                   creditModel.setDetails(rs.getString("details"));
                }
                DBConnection.getStatement().executeUpdate("update credit_note set issued_against='Customer',credit_opt='Sales Return',credit_date='"+sales.getSalesReturnDate()+"',name='"+sales.getCustomerName()+"',amount="+sales.getTotalAmount()+",details='"+creditModel.getDetails()+"' where credit_note_no = '"+creditModel.getCreditNoteNumber()+"'");
                }                
            } catch (Exception ex) {               
                String ss = " Class : SalesDAO  Method   : insertCreditNoteNo Exception :" + ex.getMessage();
                log.debug(ss);
            }
        return creditModel;
    
    }

    //Update credit note table in case of sales return
    @Override
    public Boolean updateCreditDetails(String details,String creditNoteNo)
    {
        Boolean updateFlag=false;
        try{
            
            DBConnection.getStatement().executeUpdate("update credit_note set details ='"+details+"' where credit_note_no = '"+creditNoteNo+"'");
            updateFlag = true;

        }
        catch(Exception ex){
               
                String ss = " Class : SalesDAO  Method   : updateCreditDetails Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return updateFlag;

    }


    //To load current date sales in Sales maintenance form on clicking Maintenance Submenu
    @Override
    public SalesModel loadSalesMaintain(String nowDate)
    {
        ResultSet rs=null;
        SalesModel sales = new SalesModel();
        SalesModel salesitems;
        List salesList = new ArrayList();
        try{
            rs = DBConnection.getStatement().executeQuery("select bill_no, bill_date,bill_type, cust_name,total_items, total_qty,total_amount from sales_maintain_bill where bill_date = '" + nowDate + "' order by bill_no desc");
            rs.last();
            int cnt = rs.getRow();            
            
            rs.beforeFirst();
            sales.setRowCount(cnt);
            while(rs.next()){
                salesitems = new SalesModel();
                salesitems.setBillNumber(rs.getString("bill_no"));                
                salesitems.setBillDate(rs.getString("bill_date"));
                salesitems.setBillType(rs.getString("bill_type"));
                salesitems.setCustomerName(rs.getString("cust_name"));
                salesitems.setTotalItems(rs.getInt("total_items"));
                salesitems.setTotalQuantity(rs.getInt("total_qty"));
                salesitems.setTotalAmount(rs.getDouble("total_amount"));                
                salesList.add(salesitems);
            }
            sales.setListofitems(salesList);
        }
        catch(Exception ex){
               
                String ss = " Class : SalesDAO  Method  : loadSalesMaintain Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return sales;
    }

    //To load sales values in Sales maintenance form based on date, bill number search
    @Override
    public SalesModel loadSalesMaintain(String option,String value)
    {
        ResultSet rs=null;
        SalesModel sales = new SalesModel();
        SalesModel salesitems;
        List salesList = new ArrayList();
        try {
            rs = DBConnection.getStatement().executeQuery("select bill_no, bill_date,bill_type, cust_name,total_items, total_qty,total_amount from sales_maintain_bill where " + option + " like '" + value + "%' order by bill_no desc ");
            rs.last();
            int cnt = rs.getRow();            
            rs.beforeFirst();
            sales.setRowCount(cnt);
            while(rs.next()){
                salesitems = new SalesModel();
                salesitems.setBillNumber(rs.getString("bill_no"));                
                salesitems.setBillDate(rs.getString("bill_date"));
                salesitems.setBillType(rs.getString("bill_type"));
                salesitems.setCustomerName(rs.getString("cust_name"));
                salesitems.setTotalItems(rs.getInt("total_items"));
                salesitems.setTotalQuantity(rs.getInt("total_qty"));
                salesitems.setTotalAmount(rs.getDouble("total_amount"));
                salesList.add(salesitems);
            }
            sales.setListofitems(salesList);
        }
        catch(Exception ex){
               
                String ss = " Class : SalesDAO  Method  : loadSalesMaintain Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return sales;
    }


    //Dummy Bill Loading table values
    @Override
        public Vector loadDummyStockTable(String val) {
        ResultSet rs = null;
        Vector temp = null;
        Vector data = new Vector();
        PurchaseDAO purchase = new PurchaseDAO();
        try {
            rs = DBConnection.getStatement().executeQuery("CALL pro_getDrugTables('" + val + "')");
            while(rs.next()){
                temp = new Vector();
                String vt = "0";
                    String mfg = "";
                    String code = rs.getString("itemcode").trim();
                    double mrp = rs.getDouble("mrp");
                    int pack = purchase.getPackingValue(code);
                    String unitmrp = Value.Round(mrp/pack);
                    if (rs.getString("vat") == null || rs.getString("vat").equals("0.00")); else {
                        vt = rs.getString("vat").trim();
                    }
                    mfg = rs.getString("mfgname").trim();
                    temp.addElement(rs.getString("itemname").trim());
                    temp.addElement(code);
                    temp.addElement(mfg);
                    temp.addElement(rs.getString("dosage").trim());
                    temp.addElement(unitmrp);
                    temp.addElement(vt);
                    temp.addElement(rs.getString("formulation"));
                    data.addElement(temp);
            }

        } catch(Exception ex) {
                String ss = " Class : SalesDAO  Method   : loadDummyStockTable Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return data;
    }


    //Loading table values for cash,credit,card and counter sales bills
    @Override
    public Vector loadStockTable (String val) {
     ResultSet rs = null;
        Vector temp = null;
        Vector data = new Vector();
        try {
              // this condition only applicable for sharma medicals only
            rs = DBConnection.getStatement().executeQuery("CALL pro_getStockTables('" + val + "')");
            while(rs.next()){
                    temp = new Vector();
                    String unitPrice = "0";
                    String vat = "0";
                    String mfg = "";
                    String batch = "";
                    String edate = DateUtils.now("MM-yy");
                    edate = rs.getString("expiry_date");
                    String code = rs.getString("item_code").trim();
                    String mrp = rs.getString("mrp").trim();
                    String sellingPrice = rs.getString("selling_price").trim();
                    String bat = rs.getString("batch_no").trim();
                    int stkQty = rs.getInt("stkqty");
                    mfg = rs.getString("mfgname").trim();                    
                    if (rs.getString("vat") == null || rs.getString("vat").equals("0.00")); else {
                        vat = rs.getString("vat").trim();
                    }                   
                    
                    if (sellingPrice == null || sellingPrice.equals("")) {
                        if (mrp.equalsIgnoreCase("0") || mrp.equalsIgnoreCase("0.0")) {
                            unitPrice = "0";
                        } else {
                            unitPrice = mrp;
                        }
                    } else {
                        unitPrice = sellingPrice;
                    }
                    if (bat == null || bat.equals("")); else {
                        batch = bat;
                    }
                    temp.addElement(rs.getString("itemname").trim());
                    temp.addElement(code);
                    temp.addElement(mfg);
                    temp.addElement(rs.getString("dosage").trim());
                    temp.addElement(batch);
                    temp.addElement(stkQty);
                    temp.addElement(Double.parseDouble(unitPrice));
                    temp.addElement(mrp);
                    temp.addElement(vat);
                    temp.addElement(edate);
                    temp.addElement(rs.getString("formulation"));
                    temp.addElement(rs.getString("ban_flag_id"));
                    temp.addElement(rs.getString("rack"));
                    temp.addElement(rs.getString("shelf"));
                    temp.addElement(rs.getString("minstock"));
                    temp.addElement(rs.getString("packing"));
                    temp.addElement(rs.getDouble("sales_discount"));
                    temp.addElement(rs.getDouble("purchase_price"));
                    data.addElement(temp);
            }
        } catch(Exception ex) {
                String ss = "Class : SalesDAO  Method : loadStockTable_1 Exception :" + ex.getMessage();
                log.debug(ss);
        }

        return data;
    }

    //to load values from Prescription to Sales
     @Override
    public SalesModel getStockItem(String val,double qty) {
        ResultSet rs = null ;
        SalesModel sm =null ;
        List<SalesModel> list ;
        try {
            String qry = "CALL pro_getStockDetails('" + val + "','"+qty+"')";            
            rs = DBConnection.getStatement().executeQuery(qry);
            if(rs != null){
                list = new ArrayList<SalesModel>();            
                while(rs.next()){
                        sm = new SalesModel();
                        sm.setModuleType("importSales");
                        String unitPrice = "0";
                        String vat = "0";
                        String mfg = "";
                        String batch = "";
                        String edate = DateUtils.now("mmm-yyyy");
                        edate = rs.getString("expiry_date");                        
                        String code = rs.getString("item_code").trim();
                        String mrp = rs.getString("mrp").trim();
                        String sellingPrice = rs.getString("selling_price").trim();
                        String bat = rs.getString("batch_no").trim();
                        int stkQty = rs.getInt("stkqty");
                        mfg = rs.getString("mfgname").trim();

                        if (rs.getString("vat") == null || rs.getString("vat").equals("0.00")); else {
                            vat = rs.getString("vat").trim();
                        }

                        if (sellingPrice == null || sellingPrice.equals("")) {
                            if (mrp.equalsIgnoreCase("0") || mrp.equalsIgnoreCase("0.0")) {
                                unitPrice = "0";
                            } else {
                                unitPrice = mrp;
                            }
                        } else {
                            unitPrice = sellingPrice;
                        }
                        if (bat == null || bat.equals("")); else {
                            batch = bat;
                        }
                        sm.setItemName(rs.getString("itemname").trim()+"_"+rs.getString("dosage").trim());
                        sm.setItemCode(code);
                        sm.setManufacturerName(mfg);
                        sm.setBatchNumber(batch);                        
                        sm.setQuantity((int) qty);                        
                        sm.setUnitPrice(Double.parseDouble(unitPrice));
                        sm.setMrp(rs.getDouble("mrp"));
                        sm.setUnitPrice(rs.getDouble("selling_price"));
                        sm.setUnitVAT(rs.getDouble("vat"));
                        sm.setExpiryDate(edate);
                        sm.setFormulation(rs.getString("formulation"));
                        sm.setUpdateFlag(rs.getString("ban_flag_id"));//use UbdateFlag instead baanedFlag 
                        sm.setPacking(rs.getInt("packing"));
                        sm.setUnitDiscount(rs.getDouble("sales_discount"));
                        list.add(sm);
                }
            }

        } catch(Exception ex) {
                String ss = "Class : SalesDAO  Method : getStockItem Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return sm;
    }

    //Loading table values for cash,credit,card and counter sales bills
    @Override
    public Vector loadStockTable(String val, Object dos) {
        ResultSet rs = null;
        Vector temp = null;
        Vector data = new Vector();
        try {
                rs = DBConnection.getStatement().executeQuery("CALL pro_getStockTables('" + val + "')");
                while (rs.next()) {
                    temp = new Vector();
                    String name = rs.getString("itemname").trim();
                    String dosage = rs.getString("dosage").trim();
                    int stkQty = rs.getInt("stkqty");
                    if (name.equals(val.trim()) && dosage.equals(dos) && stkQty > 0) {
                        String up = "0";
                        String vt = "0";
                        String batch = "";
                        String edate = DateUtils.now("MMM-yyyy");
                        String mfg = "";
                        String code = rs.getString("item_code").trim();
                        String mrp = rs.getString("mrp").trim();
                        String s = rs.getString("selling_price").trim();
                        String bat = rs.getString("batch_no").trim();
                        edate = rs.getString("expiry_date");
                        if (rs.getString("vat") == null || rs.getString("vat").equals("0.00")); else {
                            vt = rs.getString("vat").trim();
                        }
                        mfg = rs.getString("mfgname").trim();
                        temp.addElement(name);
                        if (s == null || s.equals("")) {
                            if (mrp.equalsIgnoreCase("0") || mrp.equalsIgnoreCase("0.0")) {
                                up = "0";
                            } else {
                                up = mrp;
                            }
                        } else {
                            up = s;
                        }
                        if (bat == null || bat.equals("")); else {
                            batch = bat;
                        }
                        temp.addElement(code);
                        temp.addElement(mfg);
                        temp.addElement(dosage);
                        temp.addElement(batch);
                        temp.addElement(stkQty);
                        temp.addElement(Double.parseDouble(up));
                        temp.addElement(mrp);
                        temp.addElement(vt);
                        temp.addElement(edate);
                        temp.addElement(rs.getString("formulation"));
                        temp.addElement(rs.getString("ban_flag_id"));
                        data.addElement(temp);
                    } else {
                        continue;
                    }
                }
        } catch(Exception ex) {
                String ss = "Class : SalesDAO  Method   : loadStockTable_2 Exception :" + ex.getMessage();
                log.debug(ss);
        }        
        return data;
    }


    //Load substitute drug values in sales on pressing F11
    @Override
    public Vector loadSubstituteDrug(String val, int purRate, int stk){
        ResultSet rs = null;
        Vector temp = null;
        Vector data = new Vector();
        try {
                rs = DBConnection.getStatement().executeQuery("CALL pro_getSubstitutes(" + val + "," + purRate + "," + stk + ")");
                rs.last();
                int rr = rs.getRow();
                rs.beforeFirst();
                if (rr > 0) {
                    while (rs.next()) {
                        temp = new Vector();
                        String up = "0";
                        String vt = "0";
                        String batch = "";
                        String edate = DateUtils.now("MM-yy");
                        String mfg = "";
                        String code = rs.getString("item_code").trim();
                        String mrp = rs.getString("mrp").trim();
                        String s = rs.getString("selling_price").trim();
                        String bat = rs.getString("batch_no").trim();
                        int stkQty = rs.getInt("stkqty");
                        edate = DateUtils.normalFormatExpDate(rs.getDate("expiry_date"));
                        if (rs.getString("vat") == null || rs.getString("vat").equals("0.00")); else {
                            vt = rs.getString("vat").trim();
                        }
                        mfg = rs.getString("mfgname").trim();
                        temp.addElement(rs.getString("itemname").trim());
                        if (s == null || s.equals("")) {
                            if (mrp.equalsIgnoreCase("0") || mrp.equalsIgnoreCase("0.0")) {
                                up = "0";
                            } else {
                                up = mrp;
                            }
                        } else {
                            up = s;
                        }
                        if (bat == null || bat.equals("")); else {
                            batch = bat;
                        }
                        temp.addElement(code);
                        temp.addElement(mfg);
                        temp.addElement(rs.getString("dosage"));
                        temp.addElement(rs.getString("generic"));
                        temp.addElement(batch);
                        temp.addElement(stkQty);
                        temp.addElement(Double.parseDouble(up));
                        temp.addElement(mrp);
                        temp.addElement(vt);
                        temp.addElement(edate);
                        temp.addElement(rs.getString("formulation"));
                        temp.addElement(rs.getString("ban_flag_id"));
                        data.addElement(temp);                        
                    }
            }
        }
        catch(Exception ex) {
                String ss = " Class : SalesDAO  Method   : loadSubstituteDrug Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return data;
    }

    //sales return and sales Adjustment datas loading table
    @Override
    public Vector loadSalesDetails(String val,int no) {
        ResultSet rs = null;
        Vector temp = null;
        Vector data = new Vector();
        CommonDAO commonDAO = new CommonDAO();
        try {        
                rs = DBConnection.getStatement().executeQuery( "CALL pro_getSalesDetails('" + val + "'," + no + ")");
                while (rs.next()) {
                    temp = new Vector();
                    boolean bo = true;
                    String iname = rs.getString("item_name");
                    String icode = rs.getString("item_code");
                    String batch = rs.getString("batch_no");
                    temp.addElement(icode);
                    temp.addElement(iname);
                    temp.addElement(rs.getString("formulation"));
                    temp.addElement(rs.getInt("qty"));
                    temp.addElement(batch);
                    temp.addElement(DateUtils.normalFormatExpDate(rs.getDate("expiry_date")));
                    temp.addElement(rs.getDouble("unit_price"));
                    temp.addElement(Value.Round(rs.getString("mrp")));
                    temp.addElement(Value.Round(rs.getString("unit_discount")));
                    temp.addElement(Value.Round(rs.getString("unit_vat")));
                    temp.addElement(Boolean.FALSE);
                    if(!val.equals("ADJUSTMENT")) {
                        int ses = commonDAO.getReturnCompare(val,icode,iname,batch);
                        if (ses > 0) {
                            bo = false;
                        }
                    }
                    else {                        
                        bo=true;
                    }
                    temp.addElement(rs.getInt("adj_id"));                    
                    temp.addElement(bo);
                    data.addElement(temp);
                }
        }
        catch(Exception ex) {               
                String ss = " Class : SalesDAO  Method   : loadSalesDetails Exception :" + ex.getMessage();
                log.debug(ss);
        }
        return data;
    }

    //List bill numbers in Sales Return
    @Override
    public List<String> billNum(String name) {
        List<String> billList = new ArrayList<String>();
        try {
            ResultSet rs = null;
            if (name.equals("") || name == null) {
                rs = DBConnection.getStatement().executeQuery("select bill_no from sales_maintain_bill order by bill_no");
            } else {
                rs = DBConnection.getStatement().executeQuery("select bill_no from sales_maintain_bill where bill_no like'" + name + "%' order by bill_no");
            }
            while (rs.next()) {
                billList.add(rs.getString("bill_no"));
            }

        } catch (Exception ex) {
            String ss = " Class : SalesDAO  Method  : billNum     Exception :" + ex.getMessage();
            log.debug(ss);
        }
        return billList;
    }

    //Load previous bill number in Sales
    @Override
    public List<String> previousBillNumber(String name) {
        List<String> billList = new ArrayList<String>();
        try {
            ResultSet rs = null;
            String qry1="";
            String custName = "";
            Pattern p = Pattern.compile("^[0-9]+$");
            if (name != null && name.trim().length() > 2) {
                Matcher m = p.matcher(name.substring(0, 2));
                boolean matchFound = m.matches();
                if (matchFound) {
                    qry1="SELECT cust_name FROM cust_information where mobile_no = '"+name+"'";
                    rs = DBConnection.getStatement().executeQuery(qry1);
                    if(rs!=null && rs.next()){
                        custName = rs.getString("cust_name");
                    }
                } else {
                    custName = "";
                }
            }
            if (name.equals("") || name == null) {
                qry1 = "select bill_no,cust_name from sales_maintain_bill where bill_type!='Account' order by bill_no desc";
            } else if(custName.length() > 1 ){
                qry1 = "select bill_no,cust_name from sales_maintain_bill where bill_type!='Account' and cust_name = '" + custName + "' order by bill_no desc";
            } else {
                qry1 = "select bill_no,cust_name from sales_maintain_bill where bill_type!='Account' and bill_no like '" + name + "%' order by bill_no desc";
            }
            rs= null;
            rs = DBConnection.getStatement().executeQuery(qry1);
            while (rs.next()) {
                billList.add(rs.getString("bill_no")+"-"+rs.getString("cust_name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String ss = " Class : SalesDAO  Method  : billNum     Exception :" + ex.getMessage();
            log.debug(ss);
        } finally {

        }   
        return billList;
    }
   
    //Load sales bill details in sales return on selecting a bill no
    @Override
    public SalesModel getBillDetails(String no, String tName) {
        String tabName = "sales_" + tName;
        String query = "select * from " + tabName + " where bill_no='" + no + "'";
        SalesModel sales=new SalesModel();
        try {
            ResultSet rs = DBConnection.getStatement().executeQuery(query);
            while (rs.next()) {
                if (tabName.equalsIgnoreCase("sales_cards_bill")) {
                    sales.setPaymentMode(rs.getString("payment_mode"));
                    sales.setCardNumber(rs.getString("card_no"));
                    sales.setCardHolderName(rs.getString("card_holders_name"));
                    sales.setBankName(rs.getString("bank_name"));
                    sales.setCardExpiry(rs.getString("card_expiry"));
                }
                sales.setBillDate(rs.getString("bill_date"));
                sales.setCustomerName(rs.getString("cust_name"));
                sales.setDoctorName(rs.getString("doctor_name"));
                sales.setTotalDiscount(rs.getDouble("total_discount"));
                sales.setTotalVAT(rs.getDouble("total_vat"));
                sales.setTotalAmount(rs.getDouble("total_amount"));
                sales.setPaidAmount(rs.getDouble("paid_amount"));
                sales.setBalanceAmount(rs.getDouble("balance_amount"));
                sales.setTotalQuantity(rs.getInt("total_qty"));
                sales.setTotalItems(rs.getInt("total_items"));
            }
        } catch (Exception ex) {
            String ss = " Class : SalesDAO  Method  : getBillDetails     Exception :" + ex.getMessage();
            log.debug(ss);
        }
        return sales;
    }

    //Load sales return numbers list in edit
    @Override
    public List<String> srBillNum() {
        List salesReturnNo = new ArrayList();
        try {
            ResultSet rs = DBConnection.getStatement().executeQuery("select distinct(sales_return_no) from sales_return order by sales_return_no");
            while (rs.next()) {
                salesReturnNo.add(rs.getString("sales_return_no"));
            }

        } catch (Exception ex) {
            String ss = " Class : SalesDAO Method : srBillNum()     Exception :" + ex.getMessage();
            log.debug(ss);
        }
        return salesReturnNo;
    }

    //Load sales return details in edit on selecting a return number
    @Override
    public SalesModel srBillDetails(String no) {
        String billNO = no;
        SalesModel sales=new SalesModel();
        SalesModel salesTableItems;
        ResultSet rs=null;
        List salesList = new ArrayList();
        try {
            rs = DBConnection.getStatement().executeQuery("select * from sales_return where sales_return_no='" + billNO + "'");            
            while (rs.next()) {
                sales.setBillNumber(rs.getString("bill_no"));
                sales.setBillDate(rs.getString("bill_date"));
                sales.setSalesReturnDate(rs.getString("sales_return_date"));
                sales.setCustomerName(rs.getString("cust_name"));
                sales.setBillType(rs.getString("bill_type"));
                sales.setDoctorName(rs.getString("doctor_name"));
                sales.setTotalDiscount(rs.getDouble("total_discount"));
                sales.setTotalVAT(rs.getDouble("total_vat"));
                sales.setTotalAmount(rs.getDouble("total_amount"));
                sales.setPaidAmount(rs.getDouble("paid_amount"));
                sales.setBalanceAmount(rs.getDouble("balance_amount"));
                sales.setTotalQuantity(rs.getInt("total_qty"));
                sales.setTotalItems(rs.getInt("total_items"));
                if (sales.getBillType().equalsIgnoreCase("Credit/Debit Card")) {
                    sales.setPaymentMode(rs.getString("payment_mode"));
                    sales.setCardNumber(rs.getString("card_no"));
                    sales.setCardHolderName(rs.getString("card_holders_name"));
                    sales.setBankName(rs.getString("bank_name"));
                    sales.setCardExpiry(rs.getString("card_expiry"));
                }
            }
            String query1 = "select item_code,item_name,formulation,qty,batch_no,unit_price,expiry_date,mrp,unit_discount,unit_vat,sub_total,adj_id from sales_return where sales_return_no='" + billNO + "'";
            ResultSet rs1 = DBConnection.getStatement().executeQuery(query1);
            
            rs1.last();
            int rowCount = rs1.getRow();
            rs1.beforeFirst();
            while (rs1.next()) {
                  salesTableItems = new SalesModel();
                  salesTableItems.setItemCode(rs1.getString("item_code"));
                  salesTableItems.setItemName(rs1.getString("item_name"));
                  salesTableItems.setFormulation(rs1.getString("formulation"));
                  salesTableItems.setQuantity(rs1.getInt("qty"));
                  salesTableItems.setBatchNumber(rs1.getString("batch_no"));
                  salesTableItems.setReturnExpiryDate(rs1.getString("expiry_date"));
                  salesTableItems.setUnitPrice(rs1.getDouble("unit_price"));
                  salesTableItems.setMrp(rs1.getDouble("mrp"));
                  salesTableItems.setUnitDiscount(rs1.getDouble("unit_discount"));
                  salesTableItems.setUnitVAT(rs1.getDouble("unit_vat"));
                  salesTableItems.setSubTotal(rs1.getDouble("sub_total"));
                  salesTableItems.setTotalItems(rs1.getInt("adj_id"));
                  salesList.add(salesTableItems);
                  sales.setListofitems(salesList);

            }

        } catch (Exception ex) {

            String ss =" Class : SalesDAO Method : srBillDetails     Exception :"+ ex.getMessage();
             log.debug(ss);
        }
        return sales;
    }

    //Stock update for Temp Stock
    @Override
    public boolean updateStock(StockModel stock,String type) {
        boolean  updateFlag = false;
        try {
            if(type.equals("update")){
            DBConnection.getStatement().executeUpdate("update stock_statement set qty='" + stock.getStock_qty() + "' where item_code='"+stock.getStock_itemCode()+"' and batch_no='" + stock.getStock_batchNo() + "'");
            DBConnection.getStatement().executeUpdate("insert into stock_register values(now(),'"+stock.getStock_itemCode()+"','"+stock.getStock_itemName()+"','"+stock.getStock_batchNo()+"','"+stock.getStock_minQty() +"','"+stock.getStock_minQty()+"','"+(stock.getStock_qty() +stock.getStock_minQty())+"','Temporary Stock','')");
            }
            else {
            String expDate =DateUtils.changeFormatExpDate(stock.getStock_expiryDate())+"-01";
            DBConnection.getStatement().executeUpdate("INSERT INTO  stock_statement (item_code ,item_name,batch_no ,qty,packing ,expiry_date ,mrp ,selling_price ,formulation,stock_date,ban_flag_id) VALUES ('" + stock.getStock_itemCode() + "','" + stock.getStock_itemName()+ "','" + stock.getStock_batchNo() + "','" + stock.getStock_qty() + "','" +stock.getStock_packing() + "','" + expDate + "','" + stock.getStock_sellingPrice() + "','" + stock.getStock_sellingPrice() + "','" + stock.getStock_formulation() + "',concat(curdate(),\' \',curtime()),'0')");
            DBConnection.getStatement().executeUpdate("insert into stock_register values(now(),'"+stock.getStock_itemCode()+"','"+stock.getStock_itemName()+"','"+stock.getStock_batchNo()+"','0','"+stock.getStock_qty()+"','"+stock.getStock_qty()+"','Temporary Stock','')");
            }
           updateFlag =true;
        }
        catch(Exception ex) {
            String ss =" Class : SalesDAO Method : updateStock     Exception :"+ ex.getMessage();
            log.debug(ss);
        }
        return updateFlag;
    }


    //Load sales bill details on selecting previous bill number
    @Override
    public SalesModel loadEditTable(String billNumber, String type) {
     ResultSet rs =null;
     SalesModel salesItems;
     SalesModel sales = new SalesModel();
     List salesList = new ArrayList();
     try {
          if(type.equalsIgnoreCase("cash")) {
          rs = DBConnection.getStatement().executeQuery("select * from sales_cash_bill where bill_no ='" + billNumber + "' ");
          } else if(type.equalsIgnoreCase("credit")) {
          rs = DBConnection.getStatement().executeQuery("select * from sales_credit_bill where bill_no ='" + billNumber + "' ");
          } else if(type.equalsIgnoreCase("counter")) {
          rs = DBConnection.getStatement().executeQuery("select *,0 as adj_id from sales_accounts where bill_no ='" + billNumber + "' ");
          } else if(type.equalsIgnoreCase("cards")) {
          rs = DBConnection.getStatement().executeQuery("select * from sales_cards_bill where bill_no ='" + billNumber + "' ");
          }       
          while(rs.next()) {
            salesItems = new SalesModel();
            sales.setBillNumber(rs.getString("bill_no"));
            sales.setBillDate(DateUtils.normalFormatDate(rs.getDate("bill_date")));
            sales.setCustomerName(rs.getString("cust_name"));
            sales.setDoctorName(rs.getString("doctor_name"));
            sales.setTotalQuantity(rs.getInt("total_qty"));
            sales.setTotalItems(rs.getInt("total_items"));
            sales.setTotalDiscount(rs.getDouble("total_discount"));
            sales.setTotalVAT(rs.getDouble("total_vat"));
            sales.setPaidAmount(rs.getDouble("paid_amount"));
            sales.setBalanceAmount(rs.getDouble("balance_amount"));
            sales.setTotalAmount(rs.getDouble("total_amount"));
            salesItems.setItemCode(rs.getString("item_code"));
            salesItems.setItemName(rs.getString("item_name"));
            salesItems.setManufacturerName(rs.getString("mfr_name"));
            salesItems.setFormulation(rs.getString("formulation"));
            salesItems.setBatchNumber(rs.getString("batch_no"));
            salesItems.setQuantity(rs.getInt("qty"));
            salesItems.setReturnExpiryDate(rs.getString("expiry_date"));
            salesItems.setUnitPrice(rs.getDouble("unit_price"));
            salesItems.setMrp(rs.getDouble("mrp"));
            salesItems.setUnitDiscount(rs.getDouble("unit_discount"));
            salesItems.setUnitVAT(rs.getDouble("unit_vat"));
            salesItems.setSubTotal(rs.getDouble("sub_total"));
            salesItems.setTotalItems(rs.getInt("adj_id"));
            salesList.add(salesItems);
          }
          sales.setListofitems(salesList);        
     }
     catch(Exception ex) {           
            String ss =" Class : SalesDAO Method : loadEditTable     Exception :"+ ex.getMessage();
            log.debug(ss);
     }
     return sales;
    }

    //Print Sales Bill
    @Override
    public JasperPrint jasperPrint(String billno, String billmodel,JasperReport jasperReport) {
        JasperPrint jasperPrint = null;
        try {
            //File reportSource = new File("printerfiles/Print.jasper");
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            HashMap params1 = new HashMap();
            params1.put("billno", billno);
            params1.put("billmodel", billmodel); 

            // Guru Pharmacy

//           if(billtype=="CustomizeA5" | billtype.equals("CustomizeA5"))
//            {

//            params1.put("billmodel", billmodel.concat("A5"));
//            }
//            else
//            {
//             params1.put("billmodel", billmodel);
//            }
            //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportSource);
            jasperPrint = JasperFillManager.fillReport(jasperReport, params1, DBConnection.getStatement().getConnection());
//            JasperPrintManager.printReport(jasperPrint, false);
        } catch (Exception ex) {
            String ss =" Class : SalesDAO Method : jasperPrint     Exception :"+ ex.getMessage();
            log.debug(ss);
        }
        return jasperPrint;
    }


    //Alert in sales for credit customers exceeding the limit
    @Override
    public double getCreditLimit(String custName){
        double creditLimit = 0.00;
        try {
        ResultSet rs = DBConnection.getStatement().executeQuery("select credit_limit from cust_information where cust_name ='"+custName+"'");
        while(rs.next()){
            creditLimit = rs.getDouble("credit_limit");
        }
        }
        catch(Exception e){
             String ss =" Class : SalesDAO Method : getCreditLimit Exception :"+ e.getMessage();
            log.debug(ss);
        }
        return creditLimit;
    }

    //Display Emp Code in Sales Maintenance
    @Override
    public String getSalesEmpCode(String billNo) {
        String employeeid="";
       try {
        ResultSet rs = DBConnection.getStatement().executeQuery("select employee_id from sales_maintain_bill where bill_no ='"+billNo+"'");
        while(rs.next()){
            employeeid = rs.getString("employee_id");
        }
        }
        catch(Exception e){
             String ss =" Class : SalesDAO Method : loadMaintainTable     Exception :"+ e.getMessage();
            log.debug(ss);
        }
        return employeeid;
    }

    //To highlight items having stock less than minimum stock in color
    @Override
    public int getMinStock(String query){
        int minQty = 0;
        int stk = 0;
        int returnFlag = 0;
        try {
        ResultSet rs = DBConnection.getStatement().executeQuery(query);
        while(rs.next()) {
            stk = rs.getInt("quantity");
            minQty = rs.getInt("min_qty");
            if(stk<=minQty){
            returnFlag = 1;
            }
        }
        }
        catch(Exception e){
             String ss =" Class : SalesDAO Method : getMinStock     Exception :"+ e.getMessage();
            log.debug(ss);
        }
        return returnFlag;
    }

    //Print for credit note while entering Sales Return
    @Override
    public void creditNotePrint(String creditNoteno,String returnNumber) {
        try {
            File reportSource = new File("printerfiles/creditNotePrint.jasper");
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            HashMap params1 = new HashMap();
            params1.put("creditnote", creditNoteno);
            params1.put("returnno", returnNumber);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportSource);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params1, DBConnection.getStatement().getConnection());
            JasperPrintManager.printReport(jasperPrint, false);
        } catch (Exception ex) {
            String ss =" Class : SalesDAO Method : creditNotePrint     Exception :"+ ex.getMessage();
            log.debug(ss);
        }
    }

    //Get the list of old dummy bill numbers to avoid duplication of bill numbers
    @Override
    public Boolean getDummyBillNumbers(String dummyBillNumber) {
       Boolean retFlag = false;
       try {
        ResultSet rs = DBConnection.getStatement().executeQuery("select distinct bill_no from sales_dummy_bill");
        while(rs.next()){
            if(dummyBillNumber.equalsIgnoreCase(rs.getString("bill_no"))){
               retFlag = true;
            }
        }
        }
        catch(Exception e){
             String ss =" Class : SalesDAO Method : getDummyBillNumbers     Exception :"+ e.getMessage();
            log.debug(ss);
        }
        return retFlag;
    }

    @Override
    public String[] getCustomerDetails(int custId) {


        String[] cusArray = new String[4];
        cusArray[0] = "";
        cusArray[1] = "";
        cusArray[2] = "0.0";
        cusArray[3] = "";
        int id = 0;
        try {
            String sql = "SELECT cust_address1,mobile_no,family_name,cust_type_id FROM cust_information  where cust_id='" + custId + "' and  cust_flag_id=0";
            ResultSet rs = null;
            rs = DBConnection.getStatement().executeQuery(sql);
            while (rs.next()) {
                cusArray[0] = rs.getString("cust_address1");
                cusArray[1] = rs.getString("mobile_no");
                cusArray[3] = rs.getString("family_name");
                id = rs.getInt("cust_type_id");
            }
            if (id > 0) {
                //System.out.println("id"+id);
                sql = "select customer_percentage from customer_type_mt where id='" + id + "'";
                rs = DBConnection.getStatement().executeQuery(sql);
                while (rs.next()) {
                    cusArray[2] = rs.getString("customer_percentage");
                    //System.out.println("cusArray"+cusArray[2]);
                }
            }
        } catch (Exception e) {
            String msg = "Class: SalesDAO  Method: getCustomerDetails()  = " + e.getMessage();
            log.debug(msg);
        }
        return cusArray;
    }

    @Override
    public int getCustomerId(String cusName) {
        //System.out.println("--getCustomerId cusName"+cusName);
        int cusid = 0;
        try {
            String query = "select cust_id from cust_information where cust_name='" + cusName + "' and cust_flag_id = 0";
            ResultSet rs = DBConnection.getStatement().executeQuery(query);
            while (rs.next()) {
                cusid = rs.getInt("cust_id");

            }
        } catch (Exception ex) {
            String ss = "Class : SalesDAO  Method  : getCustomerCode Exception:" + ex.getMessage();
            log.debug(ss);
        }
        
        return cusid;
    }
}


