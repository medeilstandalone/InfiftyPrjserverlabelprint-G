/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vanuston.medeil.controller;

import com.vanuston.medeil.dao.CommonDAO;
import com.vanuston.medeil.dao.SalesDAO;
import com.vanuston.medeil.dao.StockDAO;
import com.vanuston.medeil.implementation.Sales;
import com.vanuston.medeil.model.CreditNoteModel;
import com.vanuston.medeil.model.MsgReturnModel;
import com.vanuston.medeil.model.SalesModel;
import com.vanuston.medeil.model.StockModel;
import java.util.List;
import com.vanuston.medeil.util.Logger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 *
 * @author Administrator
 */
 public class SalesController extends UnicastRemoteObject implements Sales {

     public SalesController() throws RemoteException {
         super();
     }

    static Logger log=Logger.getLogger(SalesController.class,"com.vanuston.medeil.controller.SalesController");
    SalesDAO salesDAO;
    
    @Override
    public boolean insertTempStock(SalesModel sales) {
    salesDAO = new SalesDAO();
    boolean insertFlag = salesDAO.insertTempStock(sales);
    return insertFlag;
    }

    @Override
    public boolean stockAdd(SalesModel model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object viewAllRecords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Validation in Sales Table
    @Override
    public Object salesTableValidation(List<SalesModel> list,String billType,String formType, String billNumber, String tableName)
    {
        MsgReturnModel returnModel = new MsgReturnModel();
        int stockQty=0;
        int qty=0;
        int availableQty=0;
        String alert="";
        CommonDAO commonDAO= new CommonDAO();  
        try
        {
        SalesModel salesModel;
        StockDAO stock;
        // Validation for quantity and price checked in sales jtable along with comparison of stockqty and sales qty.
        for (int index = 0; index < list.size(); index++) {
            salesModel = new SalesModel();
            salesModel = list.get(index);
            int quantity = salesModel.getQuantity();
            boolean qtyFlag = checkEmptyField(quantity, 0.00,"Qty");
            if (!qtyFlag) {
                returnModel.setRowCount(index);
                returnModel.setColumnCount(4);
                if(billType.equalsIgnoreCase("counter") && formType.equalsIgnoreCase("")) {
                returnModel.setColumnCount(3);
                }
                returnModel.setReturnMessage(salesModel.getItemName()+" this medicine's quantity is empty/Zero. please enter some value.");
                returnModel.setReturnFlag(true);
            }
            Double price = salesModel.getUnitPrice();
            boolean przFlag = checkEmptyField(0,price, "Prz");
            if (!przFlag) {
                returnModel.setRowCount(index);
                returnModel.setColumnCount(7);
                if(billType.equalsIgnoreCase("counter") && formType.equalsIgnoreCase("")) {
                returnModel.setColumnCount(6);    
                }
                returnModel.setReturnMessage(salesModel.getItemName()+" this medicine's price is empty/Zero. please enter some value.");
                returnModel.setReturnFlag(true);
            }            
            if(!billType.equals("dummy")&& formType.equals("") ){
            stock=new StockDAO();
            qty = quantity;                      
            if(salesModel.getTotalItems() == 0) {
                stockQty = Integer.parseInt(stock.getStockQty(salesModel.getItemCode().toString(), salesModel.getBatchNumber().toString()));
                alert = "Stock";
            }
            else {
                int adjustedQty=Integer.parseInt(commonDAO.getQueryValue("select ABS(adjusted_stock) as qty from stock_adjustment where id='"+salesModel.getTotalItems()+"'"));
                stockQty = adjustedQty;
                alert = "Adjustment";
            }
            if(qty > stockQty) {
                 returnModel.setRowCount(index);
                 returnModel.setColumnCount(4);
                 returnModel.setReturnMessage(salesModel.getItemName()+" Qty is:"+stockQty+" .Please Enter below the "+alert+" Quantity.");
                 returnModel.setReturnFlag(true);
            }
            }
            if(billType.equals("counter")&& formType.equals("")) {
                salesDAO=new SalesDAO();
                int totTemQty    = salesDAO.getTotTempQty(index,1,list);
                if(stockQty<totTemQty) {
                   //int rowCount=salesDAO.getCntFnRow;
                   availableQty=stockQty+qty-totTemQty;
                   returnModel.setRowCount(index);
                   returnModel.setColumnCount(3);
                   returnModel.setReturnMessage(salesModel.getItemName()+"Available Stock is:"+availableQty+" .Please Enter below the Stock Quantity.");
                   returnModel.setReturnFlag(true);
                }
            }
             if(formType.equals("maintenance")){
              salesDAO=new SalesDAO();
              stock=new StockDAO();
              int soldQty;
              qty = quantity;
              if(salesModel.getTotalItems() == 0) {
                stockQty = Integer.parseInt(stock.getStockQty(salesModel.getItemCode().toString(), salesModel.getBatchNumber().toString()));
                alert = "Stock";
              }
              else  {
                int adjustedQty=Integer.parseInt(commonDAO.getQueryValue("select ABS(adjusted_stock) as qty from stock_adjustment where id='"+salesModel.getTotalItems()+"'"));
                stockQty = adjustedQty;
                alert = "Adjustment";
              }
              soldQty = Integer.parseInt(salesDAO.getSoldQty(billNumber,salesModel.getItemCode().toString(), salesModel.getBatchNumber().toString(),billType));
              int retQty = salesDAO.getSoldQty(billNumber,salesModel.getItemCode().toString(), salesModel.getBatchNumber().toString(),"","return");
              availableQty=stockQty+soldQty-retQty;              
              if(qty > availableQty) {
                   returnModel.setRowCount(index);
                   returnModel.setColumnCount(4);
                   returnModel.setReturnMessage(salesModel.getItemName()+" Available Qty is:"+availableQty+" .Please Enter below the "+alert+" Quantity.");
                   returnModel.setReturnFlag(true);
              }
            }
            if(formType.equals("return"))
            {
                salesDAO=new SalesDAO();
                int soldQty;
                qty = quantity;
                soldQty=salesDAO.getSoldQty(billNumber,salesModel.getItemCode(),salesModel.getBatchNumber(),billType,tableName);
                if(qty > soldQty)
                {
                returnModel.setRowCount(index);
                returnModel.setColumnCount(4);
                returnModel.setReturnMessage("Sales Quantity of "+salesModel.getItemName()+"is "+soldQty+ ". So enter below this quantity");
                returnModel.setReturnFlag(true);
                }
            }            
        }
        }
        catch(Exception e)
        {
          
          String ss = " Class : SalesController  Method   : salesTableValidation Exception :" + e.getMessage();
          log.debug(ss);
        }
        return returnModel;
    }

    //Check Validation using salesTableValidation method.
    //Return the msgReturnModel to SalesCash UI and display the message based on the salesTableValidation method output
    @Override
    public Object createRecord (Object object) {
        MsgReturnModel returnModel = new MsgReturnModel();
        salesDAO = new SalesDAO();
        Object insert;
        try {
            SalesModel model = (SalesModel) object;
            String formType=model.getFormType();
            MsgReturnModel tempModel = (MsgReturnModel) salesTableValidation(model.getListofitems(),model.getBillType(),formType,model.getBillNumber(),model.getTableName());

            returnModel = tempModel;
            if(!returnModel.isReturnFlag() && formType.equals("maintenance")){
                returnModel.setReturnMessage("1");
            }
            if(!returnModel.isReturnFlag() && formType.equals("")) {
                insert = salesDAO.createRecord(model);               
                if(insert.equals(true))
                    returnModel.setReturnMessage("0");
            }
            if(!returnModel.isReturnFlag() && formType.equals("return") && !model.getUpdateFlag().equals("true")) {
                insert = salesDAO.createRecord(model);               
                if(insert.equals(true))
                    returnModel.setReturnMessage("0");
            }
            if(!returnModel.isReturnFlag() && formType.equals("return") && model.getUpdateFlag().equals("true")) {
               returnModel.setReturnMessage("1");
            }
        } catch(Exception e) {            
            String ss = " Class : SalesController  Method   : CreateRecord Exception :" + e.getMessage();
            log.debug(ss);
        }
        return returnModel;
    }



    @Override
    public Object viewRecord(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deleteRecord(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Check whether Qty or Price is empty
    public boolean checkEmptyField(int testString,double priceString, String type) {
        boolean flag = false;
        try {
       int qty = 0;
       double prz = 0.0;

       if (type.equalsIgnoreCase("QTY")) {
           qty = testString;
           if (qty > 0 ) {
               flag = true;
           }
            else {
               flag = false;
            }
       } else {
           prz = priceString;
           if (prz > 0 ) {
               flag = true;
           }
            else {
               flag = false;
            }
       }
        } catch (Exception e) {
        log.debug(" Class : SalesController  Method   : checkEmptyField Exception :" + e.getMessage());
        }
        return flag;
    }

    
    
    @Override
    public Integer getAlertStatus() throws RemoteException {
        Sales salesdao=new SalesDAO();
        int i = salesdao.getAlertStatus();
        return i;
    }

    @Override
    public int getTotTempQty(int index, int i, List<SalesModel> list) {
        salesDAO = new SalesDAO();
        int totTmpQty = salesDAO.getTotTempQty(index, i, list);
        return totTmpQty;
    }

    @Override
    public String getSoldQty(String billNumber,String itemCode, String batchNumber, String billType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Integer getSoldQty(String billNumber,String itemCode, String batchNumber, String billType,String tableName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Update in Sales Maintenance
    @Override
    public Object updateRecord(Object object) {
        salesDAO = new SalesDAO();
        SalesModel model= (SalesModel) object;
        Object insert = null;
        Boolean delete =false;
        Boolean update=false;
        try {
            if(!model.getFormType().equals("return")) {
            update=salesDAO.stockAdd(model);
            }
            else {
            update=salesDAO.stockSub(model);
            }
            if(update==true) {
            delete=salesDAO.deleteRecord(object);
            }
            if(delete==true) {
            insert = salesDAO.createRecord(object);
            }            
        }
         catch(Exception e) {
            insert=false;
            String ss = " Class : SalesController  Method   : UpdateRecord Exception :" + e.getMessage();
            log.debug(ss);
        }
        return insert;
    }
    
    @Override
    public List billNum(String name){
        salesDAO = new SalesDAO();
        List billNumber=salesDAO.billNum(name);
        return billNumber;
    }

    @Override
    public List previousBillNumber(String name){
        salesDAO = new SalesDAO();
        List billNumber=salesDAO.previousBillNumber(name);
        return billNumber;
    }

    @Override
    public SalesModel getBillDetails(String no, String tName) {
        salesDAO = new SalesDAO();
        SalesModel sales;
        sales=salesDAO.getBillDetails(no, tName);
        return sales;
    }

    @Override
    public List srBillNum() {
        salesDAO = new SalesDAO();
        List SRBillNumber=salesDAO.srBillNum();
        return SRBillNumber;
    }

    @Override
     public SalesModel srBillDetails(String no) {
        salesDAO = new SalesDAO();
        SalesModel salesReturn;
        salesReturn=salesDAO.srBillDetails(no);
        return salesReturn;
    }

    @Override
    public boolean stockSub(SalesModel model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CreditNoteModel insertCreditNoteNo(SalesModel sales) {
        CreditNoteModel creditModel=new CreditNoteModel();
        salesDAO=new SalesDAO();
        creditModel = salesDAO.insertCreditNoteNo(sales);
        return creditModel;
    }

    @Override
    public Boolean updateCreditDetails(String details,String creditNoteNo) {
        Boolean updateFlag=false;
        salesDAO=new SalesDAO();
        updateFlag = salesDAO.updateCreditDetails(details,creditNoteNo);
        return updateFlag;
    }
    @Override
    public SalesModel loadSalesMaintain(String nowDate){
        SalesModel sales = new SalesModel();
        salesDAO=new SalesDAO();
        sales = salesDAO.loadSalesMaintain(nowDate);
        return sales;
     }
    @Override
    public SalesModel loadSalesMaintain(String option, String value){
        SalesModel sales = new SalesModel();
        salesDAO=new SalesDAO();
        sales = salesDAO.loadSalesMaintain(option,value);
        return sales;
     }
    @Override
    public Vector loadDummyStockTable(String val){
         salesDAO=new SalesDAO();
         Vector sales = new Vector();
         sales = salesDAO.loadDummyStockTable(val);
         return sales;
    }
    @Override
     public Vector loadStockTable(String val){
         salesDAO=new SalesDAO();
         Vector sales = new Vector();
         sales = salesDAO.loadStockTable(val);
         return sales;
    }
    @Override
     public Vector loadStockTable(String val, Object dos){
         salesDAO=new SalesDAO();
         Vector sales = new Vector();
         sales = salesDAO.loadStockTable(val,dos);
         return sales;
    }

    @Override
    public Vector loadSubstituteDrug(String val,int purRate, int stk){
         salesDAO=new SalesDAO();
         Vector sales = new Vector();
         sales = salesDAO.loadSubstituteDrug(val,purRate,stk);
         return sales;
    }

    @Override
    public Vector loadSalesDetails(String val,int no) {
         salesDAO=new SalesDAO();
         Vector sales = new Vector();
         sales = salesDAO.loadSalesDetails(val,no);
         return sales;
    }

    @Override
    public boolean updateStock(StockModel stock,String type) {
        salesDAO = new SalesDAO();
        boolean  updateFlag = salesDAO.updateStock(stock,type);
        return updateFlag;
    }

    @Override
    public SalesModel loadEditTable(String billNumber ,String type) {
        salesDAO = new SalesDAO();
        SalesModel sales = new SalesModel();
        sales =salesDAO.loadEditTable(billNumber, type);
        return sales;
    }

    @Override
    public JasperPrint jasperPrint(String billno, String billmodel,JasperReport jasperReport) {
        salesDAO = new SalesDAO();
        return salesDAO.jasperPrint(billno, billmodel, jasperReport);
    }

    @Override
    public double getCreditLimit(String custName) {
        salesDAO = new SalesDAO();
        return salesDAO.getCreditLimit(custName);
    }

    @Override
    public String getSalesEmpCode(String billNo) {
        salesDAO = new SalesDAO();
        return salesDAO.getSalesEmpCode(billNo);
    }

    @Override
    public int getMinStock(String query) throws RemoteException {
        salesDAO = new SalesDAO();
        return salesDAO.getMinStock(query);
    }

    @Override
    public void creditNotePrint(String creditNoteno,String returnNo) throws RemoteException {
        salesDAO = new SalesDAO();
        salesDAO.creditNotePrint(creditNoteno,returnNo);
    }
    @Override
    public Boolean getDummyBillNumbers(String dummyBillNumber) throws RemoteException {
        salesDAO = new SalesDAO();
        return salesDAO.getDummyBillNumbers(dummyBillNumber);
    }
    @Override
    public SalesModel getStockItem(String dName,double qty) throws RemoteException {
        salesDAO = new SalesDAO();
        return salesDAO.getStockItem(dName, qty);
    }

    @Override
    public String[] getCustomerDetails(int custId) throws RemoteException {
        salesDAO = new SalesDAO();
        return salesDAO.getCustomerDetails(custId);
    }

     @Override
     public int getCustomerId(String cusName) throws RemoteException {
         salesDAO = new SalesDAO();
         return salesDAO.getCustomerId(cusName);
     }
    
}

