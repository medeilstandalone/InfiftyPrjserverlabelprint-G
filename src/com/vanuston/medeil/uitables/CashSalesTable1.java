    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vanuston.medeil.uitables;

import com.vanuston.medeil.client.RegistryFactory;
import com.vanuston.medeil.implementation.CommonImplements;
import com.vanuston.medeil.implementation.Stock;
import com.vanuston.medeil.implementation.Sales;
import com.vanuston.medeil.model.SalesModel;
import com.vanuston.medeil.util.DecimalFormatRenderer;
import com.vanuston.medeil.util.Logger;
import com.vanuston.medeil.util.PercentageFormatRenderer;
import com.vanuston.medeil.util.RegistryConstants;
import com.vanuston.medeil.util.Value;

import java.awt.Font;
import java.awt.Color;
import java.util.Vector;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
//import javax.swing.AbstractAction;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javafx.stage.Alert;

/**
 *
 * @author Administrator
 */

public class CashSalesTable1 {

    static Logger log = Logger.getLogger(CashSalesTable1.class, "Utilities");
    public  JTable jcomp;
    public  SalesTableModel dtmodel;
    public  String thisForm = null;
    public  int initR;
    public  JScrollPane scrollPane;
    public  int qty[];
    public  double uprice[];
    public  double vat4[];
    /**
     *
     */
    public  double vat12[];
    public  double dist[];
    public  double subTot[];
    public  int stockQty;
    public  int totQty = 0;
    public  int totItems = 0;
    public  double totAmt = 0.00;
    public  double totDistAmt = 0.0;
    public  double totVATAmt4 = 0.0;
    public double totMargin=0.0;
   //- public  double totVATAmt12 = 0.0;
    public  int nullVal;
    public  KeyStroke focusOut = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
    public  KeyStroke focusOut1 = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
    public  KeyStroke callTable = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK);
    public  KeyStroke shrctF2 = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    public  KeyStroke shrctF3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    public  KeyStroke shrctF4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    public  KeyStroke shrctF5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    public  KeyStroke shrctF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    public  KeyStroke shrctF7 = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    public  KeyStroke shrctF8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    public  KeyStroke shrctF12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
    public  KeyStroke shrctCtrlAr = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK);
    public  double amt = 0.00;
    public  String alert1 = "";
    public  String itN = "";    
    
    public  GetSalesDrugData g;
    public  GetSubstituteDrugs sd;
    public boolean barcodeSelected=false;
    public boolean barcodeFocus = false;
    public String itemCode = "";
    public int k = 0;

    public  JComponent createTable(final int initRow, Object[] colName, Class[] type, boolean[] canEdit, int[] width, String form) {        
        thisForm = form;
        initR = initRow;
        final Class[] types = type;
        final boolean[] canEditCols = canEdit;
        final int[] colWidth = width;
        int len = colName.length;
        try {
            jcomp = new JTable();
            scrollPane = new JScrollPane();
            g = new GetSalesDrugData(jcomp, thisForm,"salescashinsert");
            Vector cols = new Vector();
            Vector data = new Vector();
            for (int i = 0; i < initRow; i++) {
                Vector c = new Vector();
                for (int j = 0; j < len; j++) {
                    c.addElement(null);
                }
                data.addElement(c);
            }
            for (int n = 0; n < len; n++) {
                cols.addElement(colName[n]);
            }
            try {                
                dtmodel=new SalesTableModel(data,cols,types,canEditCols);
                jcomp.setModel(dtmodel);
            } catch (Exception ex) {
                String ss = "Method: Create Table    Exception : " + ex.getMessage();
                log.debug(ss);
            }
            if (colWidth.length > 0) {
                for (int i = 0; i < len; i++) {
                    jcomp.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
                }
            }
            jcomp.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            jcomp.setSelectionMode(0);
            jcomp.getTableHeader().setBackground(new Color(226, 238, 244));
            jcomp.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            jcomp.setShowGrid(true);
            jcomp.getTableHeader().setReorderingAllowed(false);
            jcomp.getColumnModel().getColumn(12).setCellRenderer(new DecimalFormatRenderer());
            jcomp.getColumnModel().getColumn(11).setCellRenderer(new PercentageFormatRenderer());
            jcomp.getColumnModel().getColumn(9).setCellRenderer(new PercentageFormatRenderer());
            jcomp.removeColumn(jcomp.getColumnModel().getColumn(13));
            jcomp.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            jcomp.getInputMap().put(focusOut, "focusOut");
            jcomp.getInputMap().put(focusOut1, "focusOut");
            jcomp.getInputMap().put(shrctF2, "focusOut");
            jcomp.getInputMap().put(shrctF3, "focusOut");
            jcomp.getInputMap().put(shrctF4, "focusOut");
            jcomp.getInputMap().put(shrctF5, "focusOut");
            jcomp.getInputMap().put(shrctF6, "focusOut");
            jcomp.getInputMap().put(shrctF7, "focusOut");
            jcomp.getInputMap().put(shrctF8, "focusOut");
            jcomp.getInputMap().put(shrctCtrlAr, "focusOut");
            jcomp.getInputMap().put(shrctF12, "removeRow");
            jcomp.getInputMap().put(callTable, "callTable");
            jcomp.getActionMap().put("removeRow", new AbstractAction("removeRow") {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    removeRow();
                }
            });

            jcomp.getActionMap().put("focusOut", new AbstractAction("focusOut") {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    jcomp.clearSelection();
                    jcomp.transferFocus();
                }
            });
            jcomp.getActionMap().put("callTable", new AbstractAction("callTable") {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    if(barcodeSelected==false) {
                        g.refreshStockTab();
                    }
                    else {
                        g.refreshStockTab(itemCode);
                        barcodeSelected = false;                        
                    }
                }
            }); 
            jcomp.setGridColor(new java.awt.Color(204, 204, 255));
            jcomp.setCellSelectionEnabled(true);
            jcomp.setRowHeight(25);
            jcomp.setSelectionBackground(Color.LIGHT_GRAY);
            jcomp.setSelectionForeground(Color.RED);
            jcomp.revalidate();
            jcomp.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    final int i = jcomp.getSelectedRow();
                    final int j = jcomp.getSelectedColumn();
                    for (int val = 0; val < jcomp.getRowCount(); val++) {
                        if (jcomp.getModel().getValueAt(val, 4) == null); else {
                            if (jcomp.getModel().getValueAt(val, 4).equals(0)) {
                                jcomp.changeSelection(val, 4, false, false);
                            }
                        }
                    }
                    calculateMargin();
                    salesCalculations();
                }
            });
            
            jcomp.addKeyListener(new KeyAdapter() {                
                @Override
                public void keyReleased(KeyEvent e) {                    
                    final int i = jcomp.getSelectedRow();
                    final int j = jcomp.getSelectedColumn();

                    try {
                        if (i == jcomp.getModel().getRowCount() - 1) {
                            addRow();
                        }
                        CommonImplements commonController = (CommonImplements) RegistryFactory.getClientStub(RegistryConstants.CommonImplements);
                        salesCalculations();
                        if (e.getKeyCode() == 10) {
                            InputMap im = jcomp.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                            KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
                            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                            im.put(enter, im.get(tab));
                        }
                        if ((j == 1 || j == 2) && e.getKeyCode() != 27) {
                            if(barcodeSelected==false) {
                                g.refreshStockTab();
                                barcodeFocus=false;
                            }
                            else {
                                g.refreshStockTab(itemCode);
                                barcodeSelected = false;
                                barcodeFocus=true;
                            }
                        }
                        alert1 = "";
                        itN = (String) jcomp.getModel().getValueAt(i, 2);

                        if (itN != null && itN.length() > 0) {
                            String expDate = "";
                            String itemCode = jcomp.getModel().getValueAt(i, 1).toString();
                            String batchNumber = jcomp.getModel().getValueAt(i, 5).toString();
                            String typeAler = commonController.getAlertType();
                            String typeVal = commonController.getAlertInterval();
                            expDate = commonController.getExpiryDate(itemCode, batchNumber,typeAler,typeVal);
                            if (expDate != null && expDate.length() > 0) {
                                alert1 = expDate;
                            } else {
                                alert1 = "";
                            }
                        }
                        if (j == 5) {
                            Stock stockController = (Stock) RegistryFactory.getClientStub(RegistryConstants.Stock);
                            int stkQty = 0;
                            stkQty = Integer.parseInt(stockController.getStockQty(jcomp.getModel().getValueAt(i, 1).toString(),jcomp.getModel().getValueAt(i, 5).toString()));;
                            int qty = 0;
                            if (jcomp.getModel().getValueAt(i, 4) == null); else {
                                qty = Integer.parseInt(jcomp.getModel().getValueAt(i, 4).toString());
                            }
                            if (qty == 0) {
                                jcomp.changeSelection(i, 4, false, false);
                            } else {
                                double sp = 0;
                                if(jcomp.getModel().getValueAt(i, 13).toString().equals("0")) {
                                    if (qty <= stkQty) {
                                        if (i == jcomp.getModel().getRowCount() - 1) {
                                            addRow();
                                        }
                                        jcomp.changeSelection(i, 9, false, false);
                                    } else {
                                        String ss = "";
                                        if (stkQty == 0 || stkQty < 0) {
                                            ss = "Please this Medicine " + jcomp.getModel().getValueAt(i, 2) + " is Out of Stock";
                                        } else {
                                            ss = "Please enter quantity less than Stock Quantity of Medicine " + jcomp.getModel().getValueAt(i, 2) + " is : " + stkQty;
                                        }                                                          
                                        Alert.inform("Sales Cash", ss);
                                        jcomp.requestFocus();
                                        jcomp.changeSelection(i, 4, false, false);
                                    }
                                }                              
                            }                         
                        }                        
                        else if(i == jcomp.getModel().getRowCount() - 1 && j==10) {
                            addRow();
                        }
                        calculateMargin();
                    } catch (Exception ex) {                        
                        String ss = "Method: tablekeyReleased    Exception : " + ex.getMessage();
                        log.debug(ss);
                    }                   
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    final int i = jcomp.getSelectedRow();
                    final int j = jcomp.getSelectedColumn();
                    InputMap im = jcomp.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    try {
                        salesCalculations();
                        if (e.getKeyCode() == 10) {
                            KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
                            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                            im.put(enter, im.get(tab));
                        }
                        if (e.isActionKey()) {
                            for (int val = 0; val < jcomp.getRowCount(); val++) {
                                if (jcomp.getModel().getValueAt(val, 1) == null); else {
                                    jcomp.getModel().setValueAt(val + 1, val, 0);
                                }
                                if (jcomp.getModel().getValueAt(val, 4) == null); else {
                                    if (Integer.parseInt(jcomp.getModel().getValueAt(val, 4).toString()) == 0) {
                                        jcomp.changeSelection(val, 4, false, false);
                                        break;
                                    }
                                }
                            }
                        }
                        if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                            if (jcomp.getModel().isCellEditable(i, j)) {
                                jcomp.getModel().setValueAt("", i, j);
                            }
                        }
                        else {
                            char c = e.getKeyChar();
                            if(jcomp.getModel().isCellEditable(i, j)) {                           
                            if((c>'0' && c<='9'))
                            jcomp.getModel().setValueAt(0, i, j);                            
                        }
                        }
                        calculateMargin();
                    } catch (Exception ex) {
                        String ss = "Method: tableKeyPressed    Exception : " + ex.getMessage();
                        log.debug(ss);
                    }
                }
                @Override
                public void keyTyped(KeyEvent e) {
                    calculateMargin();
                    salesCalculations();
                }
            });
            return jcomp;
        } catch (Exception ex) {
            String ss = "Method: createTable   Exception : " + ex.getMessage();
            log.debug(ss);
        }
        return jcomp;
    }
    

    public double calculateMargin() {
         double margin = 0.0;
         try {
         double discount=0.00;
         double vat=0.00;
         double purPrice=0.00;
         double salesVat = 0.00;
         double unitPrice = 0.00;
         int i=jcomp.getSelectedRow();
         if(i!=-1) {            
            if(jcomp.getModel().getValueAt(i, 4)!=null && jcomp.getModel().getValueAt(i, 4)!="") {
                int stkQty = Integer.parseInt(jcomp.getModel().getValueAt(i, 4).toString());
                if((jcomp.getModel().getValueAt(i, 7)!=null && !jcomp.getModel().getValueAt(i, 7).toString().equals(""))) {
                    unitPrice = Double.parseDouble(jcomp.getModel().getValueAt(i, 7).toString());
                }
                if(jcomp.getModel().getValueAt(i, 9)!=null && jcomp.getModel().getValueAt(i, 9)!="") {
                    discount = Double.parseDouble(jcomp.getModel().getValueAt(i, 9).toString());
                }
                if(jcomp.getModel().getValueAt(i, 11)!=null) {
                    vat = Double.parseDouble(jcomp.getModel().getValueAt(i, 11).toString());
                }
                CommonImplements commonController = (CommonImplements) RegistryFactory.getClientStub(RegistryConstants.CommonImplements);
                String getPurcPrice=commonController.getQueryValue("select purchase_price from stock_statement where item_code='"+jcomp.getModel().getValueAt(i, 1).toString()+"' and batch_no='"+jcomp.getModel().getValueAt(i, 5).toString()+"'");
                if(!getPurcPrice.equals(""))
                purPrice=Double.parseDouble(getPurcPrice);
                double vatCalcFlag=Double.parseDouble(commonController.getQueryValue("select vat_calc_flag from drugtable where itemcode='"+jcomp.getModel().getValueAt(i, 1).toString()+"'"));
                if(vatCalcFlag==0) {
                    salesVat=Value.Round((unitPrice*vat)/(100+vat),2);
                }
                else {
                    salesVat=0;
                }
                margin=Value.Round((unitPrice-salesVat-(unitPrice*discount/100)-purPrice)*stkQty,2);
                jcomp.getModel().setValueAt(margin, i, 10);
            }
         }
         }
         catch(Exception ex) {
             ex.printStackTrace();
             String ss = "Method: calculateMargin   Exception : " + ex.getMessage();
             log.debug(ss);
         }
         return margin;
    }
    
    public  JComponent getScrollTable(JComponent jt) {
        jcomp = (JTable) jt;
        scrollPane.add(jcomp);
        scrollPane.setViewportView(jcomp);
        return scrollPane;
    }


    public void addRow() {
        DefaultTableModel model = (DefaultTableModel) jcomp.getModel();
        int row = jcomp.getRowCount();
        Vector datas = new Vector();
        try {
            for (int i = 0; i < jcomp.getModel().getColumnCount(); i++) {
                if (row == 0 && i == 0) {
                    datas.addElement(i + 1);
                } else {
                    datas.addElement(null);
                }
            }
        } catch (Exception ex) {
            String ss = "Method: addRow    Exception : " + ex.getMessage();
            log.debug(ss);
        }
        model.insertRow(jcomp.getRowCount(), datas);
    }

    public  void removeRow() {
        int i = jcomp.getSelectedRow();
        int j = jcomp.getSelectedColumn();
        int rows = jcomp.getRowCount();
        if (i != -1) {
            DefaultTableModel model = (DefaultTableModel) jcomp.getModel();
            if (rows == 1) {
                if (i == 0) {
                    model.removeRow(i);
                    resetValues();
                    for (int n = 0; n < initR; n++) {
                        addRow();
                    }
                    jcomp.changeSelection(0, 0, false, false);
                    jcomp.changeSelection(0, j, false, false);
                }
            } else {
                model.removeRow(i);
                int rows1 = jcomp.getRowCount();
                if (i + 1 != rows) {
                    for (int n = 0; n < rows1; n++) {
                        if (jcomp.getModel().getValueAt(n, 0) == null) {
                            jcomp.getModel().setValueAt(1, 0, 0);
                        } else {
                            jcomp.getModel().setValueAt(n + 1, n, 0);
                        }
                    }
                    jcomp.changeSelection(0, 0, false, false);
                    jcomp.changeSelection(i, j, false, false);
                } else {
                    jcomp.changeSelection(0, 0, false, false);
                    jcomp.changeSelection(i - 1, j, false, false);
                }
            }
            salesCalculations();

        } else {
            Alert.inform("Please select row to remove ...");
        }
    }

    public  void clearData() {
        DefaultTableModel model = (DefaultTableModel) jcomp.getModel();
        model.getDataVector().removeAllElements();
        for (int i = 0; i < initR; i++) {
            addRow();
        }
        salesCalculations();
        jcomp.revalidate();
    }

    public  List getSalesBillItems() {
        List<SalesModel> salesBillItems = new ArrayList<SalesModel>();                
        try {
        SalesModel model;
        try {
            for (int index = 0; index < jcomp.getRowCount(); index++) {
                if (!(jcomp.getModel().getValueAt(index, 1) == null)) {
                    model = new SalesModel();
                    model.setItemCode(jcomp.getModel().getValueAt(index, 1).toString());
                    model.setItemName(jcomp.getModel().getValueAt(index, 2).toString());
                    model.setFormulation(jcomp.getModel().getValueAt(index,3).toString());                    
                    if(jcomp.getModel().getValueAt(index, 4) == null || jcomp.getModel().getValueAt(index, 4).toString().equals(""))
                    {
                        model.setQuantity(0);
                    }
                    else {
                    model.setQuantity((Integer)jcomp.getModel().getValueAt(index,4));
                    }
                    model.setBatchNumber(jcomp.getModel().getValueAt(index,5).toString());
                    model.setExpiryDate(jcomp.getModel().getValueAt(index,6).toString());                    
                    if(jcomp.getModel().getValueAt(index, 7) == null || jcomp.getModel().getValueAt(index, 7).toString().equals(""))
                    {
                        model.setUnitPrice(0.00);
                    }
                    else {
                        model.setUnitPrice(Double.parseDouble(jcomp.getModel().getValueAt(index,7).toString()));
                    }                    
                    if(jcomp.getModel().getValueAt(index, 8) == null || jcomp.getModel().getValueAt(index, 8).toString().equals(""))
                    {
                        model.setMrp(0.00);
                    }
                    else {
                    model.setMrp((Double)jcomp.getModel().getValueAt(index,8));
                     }
                    if(jcomp.getModel().getValueAt(index, 9) == null || jcomp.getModel().getValueAt(index, 9).toString().equals("")) {
                        model.setUnitDiscount(0.00);
                    }
                    else {                        
                        model.setUnitDiscount(Double.parseDouble(jcomp.getModel().getValueAt(index,9).toString()));
                    }
                    model.setUnitVAT((Double)jcomp.getModel().getValueAt(index,11));
                    model.setSubTotal((Double)jcomp.getModel().getValueAt(index,12));
                    model.setTotalItems(Integer.parseInt(jcomp.getModel().getValueAt(index, 13).toString()));
                    salesBillItems.add(model);
                }
            }
        }
        catch (Exception e) {
            String ss = "Method: getSalesBillItems    Exception : " + e.getMessage();
            log.debug(ss);
        }
        } catch(Exception exception) {            
	    log.debug("Method: getSalesBillItems    Exception : " + exception.getMessage());
        }
        return salesBillItems;
    }
    
    public int getDataRowCount() {
        int rowCount = 0;
        for(int rowIndex = 0;rowIndex < jcomp.getModel().getRowCount();rowIndex++) {
           if(jcomp.getModel().getValueAt(rowIndex,1)!= null && jcomp.getModel().getValueAt(rowIndex,5)!= null && jcomp.getModel().getValueAt(rowIndex,1)!="" && jcomp.getModel().getValueAt(rowIndex,5)!="") {
           }
           else {
            rowCount = rowIndex;
            break;
           }
        }
        return rowCount;
    }
    
    public void insertPreviousBill(String billNumber) {
        try {
            if(billNumber.length()>10){
                billNumber = billNumber.split("-")[0];
            }
            Sales salesController = (Sales) RegistryFactory.getClientStub(RegistryConstants.Sales);
            Stock stockController = (Stock) RegistryFactory.getClientStub(RegistryConstants.Stock);
            List<SalesModel> salesBillItems = new ArrayList<SalesModel>();
            SalesModel salesModel;
            String billIndex = billNumber.substring(0,3);
            String billType = "";
            if(billIndex.equalsIgnoreCase("SCA")) {
                billType = "cash";
            } else if(billIndex.equalsIgnoreCase("SCR")) {
                billType = "credit";
            }
            else if(billIndex.equalsIgnoreCase("SCD")) {
                billType = "cards";
            }
            int rowCount = getDataRowCount();
            salesModel = salesController.loadEditTable(billNumber, billType);
            SalesModel sales;
            salesBillItems = salesModel.getListofitems();                 
            for (int index = 0; index < salesBillItems.size(); index++) {
                   sales = (SalesModel) salesBillItems.get(index);
                   Boolean flag = false;
                   for(int rowIndex = 0;rowIndex < rowCount && rowCount!=0; rowIndex++) {
                        if(sales.getItemCode().equals(jcomp.getModel().getValueAt(rowIndex,1).toString()) && sales.getBatchNumber().equals(jcomp.getModel().getValueAt(rowIndex,5).toString())) {
                           flag =true;
                           break;
                       }
                    }
                  int stockQuantity =0;
                  stockQuantity=Integer.parseInt(stockController.getStockQty(sales.getItemCode(),sales.getBatchNumber()));
//                  if(stockQuantity < 0) {
//                      CommonImplements commonController = (CommonImplements) RegistryFactory.getClientStub(RegistryConstants.CommonImplements);
//                      commonController.getQueryValue("select ");
//                  }
                   if(rowCount != 0 && flag==false) {
                       addRow();
                       jcomp.getModel().setValueAt(rowCount+1, rowCount,0);
                       jcomp.getModel().setValueAt(sales.getItemCode(), rowCount,1);
                       jcomp.getModel().setValueAt(sales.getItemName(), rowCount,2);
                       jcomp.getModel().setValueAt(sales.getFormulation(), rowCount,3);
                       jcomp.getModel().setValueAt(sales.getQuantity(), rowCount,4);
                       jcomp.getModel().setValueAt(sales.getBatchNumber(), rowCount,5);
                       jcomp.getModel().setValueAt(sales.getReturnExpiryDate(), rowCount,6);
                       jcomp.getModel().setValueAt(sales.getUnitPrice(), rowCount,7);
                       jcomp.getModel().setValueAt(sales.getMrp(), rowCount, 8);
                       jcomp.getModel().setValueAt(sales.getUnitDiscount(), rowCount, 9);
                       jcomp.getModel().setValueAt(0.00, rowCount,10);
                       jcomp.getModel().setValueAt(sales.getUnitVAT(), rowCount,11);
                       jcomp.getModel().setValueAt(0.00, rowCount,12);
                       jcomp.getModel().setValueAt(0, rowCount,13);
                       rowCount++;
                   }
                   else {
                       jcomp.getModel().setValueAt(rowCount+1, rowCount,0);
                       jcomp.getModel().setValueAt(sales.getItemCode(), rowCount,1);
                       jcomp.getModel().setValueAt(sales.getItemName(), rowCount,2);
                       jcomp.getModel().setValueAt(sales.getFormulation(), rowCount,3);
                       jcomp.getModel().setValueAt(sales.getQuantity(), rowCount,4);
                       jcomp.getModel().setValueAt(sales.getBatchNumber(), rowCount,5);
                       jcomp.getModel().setValueAt(sales.getReturnExpiryDate(), rowCount,6);
                       jcomp.getModel().setValueAt(sales.getUnitPrice(), rowCount,7);
                       jcomp.getModel().setValueAt(sales.getMrp(), rowCount, 8);
                       jcomp.getModel().setValueAt(sales.getUnitDiscount(), rowCount, 9);
                       jcomp.getModel().setValueAt(0.00, rowCount,10);
                       jcomp.getModel().setValueAt(sales.getUnitVAT(), rowCount,11);
                       jcomp.getModel().setValueAt(0.00, rowCount,12);
                       jcomp.getModel().setValueAt(0, rowCount,13);
                       rowCount++;
                }
            }                      
        } catch (Exception ex) {
            String ss = "Method: insertPreviousBill    Exception : " + ex.getMessage();
            log.debug(ss);
        }
    }

    public  void resetValues() {
        totQty = 0;
        totItems = 0;
        totAmt = 0.00;
        totDistAmt = 0.0;
        totVATAmt4 = 0.0;
    //-    totVATAmt12 = 0.0;
        amt = 0.00;
    }

    public  void focusSet() {
        jcomp.requestFocus();
        jcomp.changeSelection(0, 1, false, false);
        jcomp.getModel().setValueAt("1", 0, 0);
        jcomp.setCellSelectionEnabled(true);
    }

    public void focusSet(int row, int col) {
        jcomp.requestFocus();
        jcomp.changeSelection(row, col, false, false);
        jcomp.setCellSelectionEnabled(true);
    }

    public  void focusSet(int row, int col,String barcode) {
        for(int i=0; i < jcomp.getRowCount();i++) {
            if(jcomp.getModel().getValueAt(i, 1)!=null) {
                row++;
            }   
        }
        jcomp.requestFocus();
        jcomp.changeSelection(row, col, false, false);
        jcomp.setCellSelectionEnabled(true);
        g.refreshStockTab(itemCode);
        barcodeSelected = false;
        barcodeFocus=true;
    }

    public int total(int colNum) {
        int tot = 0;
        int colNo = colNum;
        for (int i = 0; i < jcomp.getModel().getRowCount(); i++) {
            if (jcomp.getModel().getValueAt(i, colNo) == null || jcomp.getModel().getValueAt(i, colNo).equals("")) {
                tot += 0;
            } else {
                tot += Double.parseDouble("" + jcomp.getModel().getValueAt(i, colNo));
            }
        }
        return tot;
    }

    public  int totalItems() {
        int tot = 0;
        for (int i = 0; i < jcomp.getModel().getRowCount(); i++) {
            if (jcomp.getModel().getValueAt(i, 2) != null || jcomp.getModel().getValueAt(i, 2) != "") {
                tot++;
            }
        }
        return tot;
    }

    public void salesCalculations() {
        int rowCount = jcomp.getRowCount();
        qty = new int[rowCount];
        uprice = new double[rowCount];
        vat4 = new double[rowCount];
        vat12 = new double[rowCount];
        dist = new double[rowCount];
        subTot = new double[rowCount];
        totDistAmt = 0;
        totVATAmt4 = 0;
        totAmt = 0;
        totQty = 0;
        totItems = 0;
        totMargin = 0;
        try {            
            for (int i = 0; i < rowCount; i++) {
                if (jcomp.getModel().getValueAt(i, 2) == null || jcomp.getModel().getValueAt(i, 2).equals("")) {
                    if (i == 0) {
                        nullVal = 0;
                        break;
                    }
                } else {
                    nullVal = 1;
                    totItems++;
                    if (jcomp.getModel().getValueAt(i, 4) == null) {
                        jcomp.changeSelection(i, 4, false, false);
                    }
                    else {
                        if (jcomp.getModel().getValueAt(i, 4).equals("")) {
                            qty[i] = 0;
                        } else {
                            qty[i] = Integer.parseInt(jcomp.getModel().getValueAt(i, 4).toString());
                        }
                        if (jcomp.getModel().getValueAt(i, 7) == null || jcomp.getModel().getValueAt(i, 7).equals("")) {
                            uprice[i] = 0.0;
                        } else {
                            uprice[i] = Double.parseDouble("" + jcomp.getModel().getValueAt(i, 7));
                        }
                        subTot[i] = Value.Round(qty[i] * uprice[i], 2);
                        jcomp.getModel().setValueAt(subTot[i], i, 12);
                        totAmt += Value.Round(subTot[i], 2);
                        if (jcomp.getModel().getValueAt(i, 9) == null || jcomp.getModel().getValueAt(i, 9).equals("")) {
                            dist[i] = 0.0;
                        } else {
                            dist[i] = Value.Round(subTot[i] * (Double.parseDouble("" + jcomp.getModel().getValueAt(i, 9)) / 100), 2);
                        }
                        totDistAmt += Value.Round(dist[i], 2);
                        if (jcomp.getModel().getValueAt(i, 11) == null || jcomp.getModel().getValueAt(i, 11).equals("")) {
                            vat4[i] = 0.0;
                            vat12[i] = 0.0;
                        } else {
                            double vatPerc = Double.parseDouble("" + jcomp.getModel().getValueAt(i, 11));
                            vat4[i] = Value.Round((qty[i]*uprice[i]*vatPerc)/(100+vatPerc), 2);
                        }
                        totVATAmt4 += Value.Round(vat4[i], 2);
                        totQty += qty[i];
                        if (jcomp.getModel().getValueAt(i, 10) == null || jcomp.getModel().getValueAt(i, 10).equals("")) {
                        totMargin += 0.0;
                        }
                        else {
                            totMargin+=Value.Round(Double.parseDouble(jcomp.getModel().getValueAt(i, 10).toString()),2);
                      }
                    }
                }                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String ss = "Method: salesCalculations    Exception : " + ex.getMessage();
            log.debug(ss);
        }
    }

    public int adjustAmount(int adjustAmt)  {
//        int itemsOneQty;
//        int rowCount;
//        int returnFlag;
//        do {
//        itemsOneQty=0;
//        rowCount=0;
//        salesCalculations();
//        int qtyChanged[] = new int[jcomp.getModel().getRowCount()];
//        if(adjustAmt < totAmt && totAmt!=0) {
//         for (int index = 0; index < jcomp.getModel().getRowCount(); index++) {
//          if (jcomp.getModel().getValueAt(index, 2) != null && jcomp.getModel().getValueAt(index, 2) != "" && jcomp.getModel().getValueAt(index, 2) != "null" ) {
//            rowCount++;
//            int oldQty = Integer.parseInt(jcomp.getModel().getValueAt(index,4).toString());
//            double oldUnitPrice = Double.parseDouble(jcomp.getModel().getValueAt(index,7).toString());
//
//            if(oldQty > 1) {
//                qtyChanged[index] = oldQty-1;
//                if(qtyChanged[index]==0) qtyChanged[index]=1;
//                jcomp.getModel().setValueAt(qtyChanged[index], index, 4);
//                jcomp.getModel().setValueAt(qtyChanged[index]*oldUnitPrice,index,12);
//                salesCalculations();
//                if(totAmt <= adjustAmt) {
//                break;
//                }
//           }
//           if(oldQty==1) itemsOneQty++;
//          }
//        }
//         returnFlag = 1;
//        }
//        else if(totAmt == 0) {
//         returnFlag = 0;
//        }
//        else {
//         returnFlag = 2;
//        }
//       if(itemsOneQty == rowCount){
//           break;
//       }
//       } while(totAmt > adjustAmt);
//       return returnFlag;
        
        int returnFlag=0;
        int qtyChanged[] = new int[jcomp.getModel().getRowCount()];
        do {
            if(adjustAmt < totAmt && totAmt!=0) {
                salesCalculations();
                for (int index = 0; index < jcomp.getModel().getRowCount(); index++) {
                  double perc = adjustAmt/totAmt;
                  if (jcomp.getModel().getValueAt(index, 2) != null && jcomp.getModel().getValueAt(index, 2) != "" && jcomp.getModel().getValueAt(index, 2) != "null" ) {
                      int oldQty = Integer.parseInt(jcomp.getModel().getValueAt(index,4).toString());
                      double unitPrice = Double.parseDouble(jcomp.getModel().getValueAt(index,7).toString());
                      qtyChanged[index] = (int) (oldQty * perc);
                      jcomp.getModel().setValueAt(qtyChanged[index], index, 4);
                      jcomp.getModel().setValueAt(qtyChanged[index]*unitPrice,index,12);
                  }
                }
                returnFlag = 1;
            }
            else if(totAmt == 0) {
                 returnFlag = 0;
            }
            else {
                 returnFlag = 2;
            }
        }  while(totAmt > adjustAmt);
        return returnFlag;
    }

    public void calcDiscount(String discAmt) {
        double discPerc=0.00;
        discPerc=Value.Round(Double.parseDouble(discAmt)*100/totAmt,2);
        for (int index = 0; index < jcomp.getModel().getRowCount(); index++) {
            if (jcomp.getModel().getValueAt(index, 2) != null && jcomp.getModel().getValueAt(index, 2) != "" && jcomp.getModel().getValueAt(index, 2) != "null" ) {
            jcomp.getModel().setValueAt(discPerc, index, 9);
            }
        }
        salesCalculations();        
    }

    public void insertSalesAdjData() {
        clearData();
        new GetSalesAdjustmentDatas(jcomp, "Sales", "ADJUSTMENT").setVisible(true);
        salesCalculations();
    }

     public void setPercentageValues(Double percentage) {
        //System.out.println("setPercentageValues");
        int rowCount = jcomp.getModel().getRowCount();
        //System.out.println("rowCount:" + rowCount);
        try {
            for (int i = 0; i < rowCount; i++) {
                //if (jcomp.getModel().getValueAt(i, 13) != null && jcomp.getModel().getValueAt(i, 13) != "") {
                if (jcomp.getModel().getValueAt(i, 1) != null && jcomp.getModel().getValueAt(i, 1) != "") {
                    //System.out.println("jComp.getModel().getValueAt(i, 13):" + jcomp.getModel().getValueAt(i, 13) + ":" + percentage);
                    jcomp.getModel().setValueAt(percentage, i, 9);
                }
            }
            salesCalculations();
        } catch (Exception ex) {
            String ss = "Method: setPercentageValues    Exception : " + ex.getMessage();
            log.debug(ss);
        }
    }

}


