package com.vanuston.medeil.uitables;

import com.vanuston.medeil.client.RegistryFactory;
import com.vanuston.medeil.implementation.Sales;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.vanuston.medeil.util.CheckAllListener;
import com.vanuston.medeil.util.CheckBoxHeader;
import com.vanuston.medeil.util.Logger;
import com.vanuston.medeil.util.RegistryConstants;

/**
 *
 * @author VANEmp025
 */
public final class GetSalesReturnDatas extends javax.swing.JDialog {

    JTable jcomp;
    CheckBoxHeader jchck;
    String title;
    String inv;
    int s = 0;
    static Logger log = Logger.getLogger(GetSalesReturnDatas.class, "Utilities");

    public GetSalesReturnDatas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public GetSalesReturnDatas(javax.swing.JTable parent, String title, String inv) {
        jcomp = parent;
        this.title = title;
        this.inv = inv;
        initComponents();
        setModal(true);
        setLocationRelativeTo(null);
        loadReturnDatas(inv);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new JTable(){

        };
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View "+title.toLowerCase()+" products");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Item Code", "Item Name", "Batch No", "Qty", "Packing", "Expiry Date", "MRP", "Select"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableColumn tc = jTable1.getColumnModel().getColumn(7);
        tc.setCellEditor(jTable1.getDefaultEditor(Boolean.class));
        tc.setCellRenderer(jTable1.getDefaultRenderer(Boolean.class));
        tc.setHeaderRenderer(new CheckBoxHeader(new CheckAllListener(jTable1,7),"Select All","Deselect All"));
        jTable1.setGridColor(new java.awt.Color(204, 204, 255));
        jTable1.setRowHeight(25);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable1FocusGained(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setResizable(false);
        jTable1.getColumnModel().getColumn(1).setResizable(false);
        jTable1.getColumnModel().getColumn(2).setResizable(false);
        jTable1.getColumnModel().getColumn(3).setResizable(false);
        jTable1.getColumnModel().getColumn(4).setResizable(false);
        jTable1.getColumnModel().getColumn(5).setResizable(false);
        jTable1.getColumnModel().getColumn(6).setResizable(false);
        jTable1.getColumnModel().getColumn(7).setResizable(false);

        jButton1.setText("Insert");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("No of Rows Selected :");

        jLabel2.setText("jLabel2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1004, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(297, 297, 297)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        insertReturnDatas();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusGained
        selectData();
    }//GEN-LAST:event_jTable1FocusGained

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        selectData();
        checkEditableData();
    }//GEN-LAST:event_jTable1KeyPressed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        selectData();
        checkEditableData();
    }//GEN-LAST:event_jTable1MouseClicked
    void selectData() {
        int rCnt = jTable1.getRowCount();
        s = 0;
        for (int i = 0; i < rCnt; i++) {
            Object obj = null;
            if (title.equals("Sales")) {
                obj = jTable1.getModel().getValueAt(i, 10);
            }
            if (obj != null) {
                boolean bo = (Boolean) obj;
                if (bo) {
                    s++;
                }
            }
        }
        jLabel2.setText(String.valueOf(s));
    }

    void checkEditableData() {
        int rCnt = jTable1.getSelectedRow();
        String n = "";
        if (rCnt != -1) {
            Object obj = null;
            if (title.equals("Sales")) {
                obj = jTable1.getModel().getValueAt(rCnt, 12);
                n = "Bill Number";
            }
            String msg = "";
            if (obj != null) {
                boolean bo = (Boolean) obj;
                if (!bo) {
                    msg = "this \"" + inv + "\" " + title + " " + n + " and \"" + jTable1.getModel().getValueAt(rCnt, 1) + "\" item has already insert to " + title + " Return.\n\nPlease use edit for this item";
                    JOptionPane.showMessageDialog(jTable1, msg, title + " Return", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    void insertSalesValue() {
        try {
            int rTot = jTable1.getRowCount();
            SalesReturnTable sr = new SalesReturnTable();
             int rowCnt = Integer.parseInt(jLabel2.getText());
            int j = 0;
             if (rowCnt > 12) {
                DefaultTableModel model = (DefaultTableModel) jcomp.getModel();
                model.getDataVector().removeAllElements();
                jcomp.revalidate();
                for (int i = 0; i < rowCnt; i++) {
                    sr.addRow();
                }
            }
            for (int i = 0; i < rTot; i++) {
                boolean isSelect = (Boolean) jTable1.getModel().getValueAt(i, 10);
                boolean isEditable = (Boolean) jTable1.getModel().getValueAt(i, 12);
                if (isSelect && isEditable) {
                    String icode = jTable1.getModel().getValueAt(i, 0).toString();
                    String iname = jTable1.getModel().getValueAt(i, 1).toString();
//                  String mfg = jTable1.getModel().getValueAt(i, 2).toString();
                    String form = jTable1.getModel().getValueAt(i, 2).toString();
                    String qty = jTable1.getModel().getValueAt(i, 3).toString();
                    String batch = jTable1.getModel().getValueAt(i, 4).toString();
                    String edate = jTable1.getModel().getValueAt(i, 5).toString();
                    String price = jTable1.getModel().getValueAt(i, 6).toString();
                    String mrp = jTable1.getModel().getValueAt(i, 7).toString();
                    String udist = jTable1.getModel().getValueAt(i, 8).toString();
                    String vat = jTable1.getModel().getValueAt(i, 9).toString();
                    int adjId = Integer.parseInt(jTable1.getModel().getValueAt(i, 11).toString());
                    jcomp.getModel().setValueAt((j + 1), j, 0);
                    jcomp.getModel().setValueAt(icode, j, 1);
                    jcomp.getModel().setValueAt(iname, j, 2);
                    jcomp.getModel().setValueAt(form, j, 3);
                    jcomp.getModel().setValueAt(Integer.parseInt(qty), j, 4);
                    jcomp.getModel().setValueAt(batch, j, 5);
                    jcomp.getModel().setValueAt(edate, j, 6);
                    jcomp.getModel().setValueAt(Double.parseDouble(price), j, 7);
                    jcomp.getModel().setValueAt(Double.parseDouble(mrp), j, 8);
                    jcomp.getModel().setValueAt(Double.parseDouble(udist), j, 9);
                    jcomp.getModel().setValueAt(Double.parseDouble(vat), j, 10);
                    jcomp.getModel().setValueAt(adjId, j, 12);
                    j++;
                }
            }
            this.dispose();
            jcomp.changeSelection(0, 4, false, false);
            jcomp.requestFocus();
        } catch (Exception ex) {
            String ss = "  Method: insertSalesValue   Exception : " + ex.getMessage();
            log.debug(ss);
            
        }
    }


    void insertReturnDatas() {
        boolean bo = false;
        for (int i = 0; i < jTable1.getModel().getRowCount(); i++) {
             if (title.equals("Sales")) {
                boolean isSelect = (Boolean) jTable1.getModel().getValueAt(i, 10);
                if (isSelect) {
                    bo = isSelect;
                }
            }
        }
        if (bo) {
            if (title.equals("Sales")) {
                insertSalesValue();
            }
        } else {
            String msg = "Please select any one item to insert !";
            JOptionPane.showMessageDialog(jTable1, msg, title + " Return", JOptionPane.WARNING_MESSAGE);
        }
    }

    void loadReturnDatas(String val) {
         if (title.equals("Sales")) {
            loadSalesDatas(val);
        }
    }

    void loadSalesDatas(String val) {
        String cut = val.substring(0, 3);
        int no = 0;
        if (cut.equals("SCA")) {
            no = 1;
        } else if (cut.equals("SCR")) {
            no = 2;
        } else if (cut.equals("SCD")) {
            no = 3;
        } else if (cut.equals("SPC")) {
            no = 4;
        } else if (cut.equals("SAC")) {
            no = 5;
        }
        Vector col = new Vector();
        Vector data = new Vector();
        try {
            if (val != null) {
                col.addElement("Product Code");
                col.addElement("Product Name");
                col.addElement("Form.");
                col.addElement("Qty");
                col.addElement("Batch No");
                col.addElement("Expiry Date");
                col.addElement("Price");
                col.addElement("MRP");
                col.addElement("Dist");
                col.addElement("VAT");
                col.addElement("Select All");
                col.addElement("Adj Id");
                col.addElement("Editable");
                Sales salesController = (Sales) RegistryFactory.getClientStub(RegistryConstants.Sales);
                data = salesController.loadSalesDetails(val,no);
                jchck = new CheckBoxHeader(new CheckAllListener(jTable1, 10), "Select All", "Deselect All");
                jTable1.setModel(new DefaultTableModel(data, col) {
                    Class[] types = new Class[]{
                        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class,
                        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                        java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class,
                        java.lang.Boolean.class
                        };

                    @Override
                    public Class getColumnClass(int columnIndex) {
                        return types[columnIndex];
                    }

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        Object edit = jTable1.getModel().getValueAt(rowIndex, 12);
                        boolean s = Boolean.valueOf(edit.toString());
                        boolean ret = false;
                        if (s && columnIndex == 10) {
                            ret = true;
                        }
                        return ret;
                    }

                    @Override
                    public void fireTableCellUpdated(int row, int column) {
                        super.fireTableCellUpdated(row, column);
                        if (column == 10) {
                            selectData();
                        } else {
                            Object edit = jTable1.getModel().getValueAt(row, 10);
                            boolean s = Boolean.valueOf(edit.toString());
                            if (!s) {
                               jchck.doCheck();                               
                            }
                        }
                    }
                });
                TableColumn tc = jTable1.getColumnModel().getColumn(10);
                tc.setCellEditor(jTable1.getDefaultEditor(Boolean.class));
                tc.setCellRenderer(jTable1.getDefaultRenderer(Boolean.class));
                tc.setHeaderRenderer(jchck);
                int colWidth[]={50,140,50,40,60,60,60,60,50,50,65,0,0};
                for (int i = 0; i < col.size(); i++) {
                    jTable1.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
                }
                jTable1.removeColumn(jTable1.getColumnModel().getColumn(12));
                jTable1.removeColumn(jTable1.getColumnModel().getColumn(11));
                jTable1.revalidate();
                jTable1.repaint();
                
            }
        } catch (Exception ex) {
            String ss = "  Method: loadSalesDatas   Exception : " + ex.getMessage();
            
            log.debug(ss);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
