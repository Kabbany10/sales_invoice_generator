package com.sales.controller;

import com.sales.model.Invoice;
import com.sales.model.InvoiceTableModel;
import com.sales.model.Line;
import com.sales.model.LineTableModel;
import com.sales.view.InvoiceDialog;
import com.sales.view.InvoiceFrame;
import com.sales.view.LineDialog;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Controller implements ActionListener, ListSelectionListener {

    private InvoiceFrame frame;
    private InvoiceDialog invoiceDialogue;
    private LineDialog lineDialogue;

    public Controller(InvoiceFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();        
        switch (actionCommand) {
            case "Load File":
                loadFile();
                break;
            case "Save File":
                saveFile();
                break;
            case "Create New Invoice":
                createNewInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Create New Item":
                createNewItem();
                break;
            case "Delete Item":
                deleteItem();
                break;
            case "createInvoiceCancel":
                createInvoiceCancel();
                break;
            case "createInvoiceOK":
                createInvoiceOK();
                break;
            case "createLineOK":
                createLineOK();
                break;
            case "createLineCancel":
                createLineCancel();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = frame.getInvoiceTable().getSelectedRow();
        if (selectedIndex != -1) {           
            Invoice currentInvoice = frame.getInvoices().get(selectedIndex);
            frame.getInvoiceNumLabel().setText("" + currentInvoice.getNum());
            frame.getInvoiceDateLabel().setText(currentInvoice.getDate());
            frame.getCustomerNameLabel().setText(currentInvoice.getCustomer());
            frame.getInvoiceTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            LineTableModel linesTableModel = new LineTableModel(currentInvoice.getLines());
            frame.getLineTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        try {
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fileChooser.getSelectedFile();
                Path headerPath = Paths.get(headerFile.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(headerPath);               
                ArrayList<Invoice> invoicesArray = new ArrayList<>();
                for (String headerLine : headerLines) {
                    String[] headerParts = headerLine.split(",");
                    int invoiceNum = Integer.parseInt(headerParts[0]);
                    String invoiceDate = headerParts[1];
                    String customerName = headerParts[2];
                    Invoice invoice = new Invoice(invoiceNum, invoiceDate, customerName);
                    invoicesArray.add(invoice);
                }                
                result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fileChooser.getSelectedFile();
                    Path linePath = Paths.get(lineFile.getAbsolutePath());
                    List<String> lineLines = Files.readAllLines(linePath);                  
                    for (String lineLine : lineLines) {
                        String lineParts[] = lineLine.split(",");
                        int invoiceNum = Integer.parseInt(lineParts[0]);
                        String itemName = lineParts[1];
                        double itemPrice = Double.parseDouble(lineParts[2]);
                        int count = Integer.parseInt(lineParts[3]);
                        Invoice inv = null;
                        for (Invoice invoice : invoicesArray) {
                            if (invoice.getNum() == invoiceNum) {
                                inv = invoice;
                                break;
                            }
                        }
                        Line line = new Line(itemName, itemPrice, count, inv);
                        inv.getLines().add(line);
                    }                   
                }
                frame.setInvoices(invoicesArray);
                InvoiceTableModel invoicesTableModel = new InvoiceTableModel(invoicesArray);
                frame.setInvoicesTableModel(invoicesTableModel);
                frame.getInvoiceTable().setModel(invoicesTableModel);
                frame.getInvoicesTableModel().fireTableDataChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        displayInvoices();
    }

    private void saveFile() {
        ArrayList<Invoice> invoices = frame.getInvoices();
        String headers = "";
        String lines = "";
        for (Invoice invoice : invoices) {
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for (Line line : invoice.getLines()) {
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }        
        try {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fileChooser.getSelectedFile();
                FileWriter headerFileWriter = new FileWriter(headerFile);
                headerFileWriter.write(headers);
                headerFileWriter.flush();
                headerFileWriter.close();
                result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fileChooser.getSelectedFile();
                    FileWriter lineFileWriter = new FileWriter(lineFile);
                    lineFileWriter.write(lines);
                    lineFileWriter.flush();
                    lineFileWriter.close();
                }
            }
        } catch (HeadlessException | IOException ex) {
            
        }
    }

    private void createNewInvoice() {
        invoiceDialogue = new InvoiceDialog(frame);
        invoiceDialogue.setVisible(true);
    }

    private void deleteInvoice() {
        int selectedRow = frame.getInvoiceTable().getSelectedRow();
        if (selectedRow != -1) {
            frame.getInvoices().remove(selectedRow);
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void createNewItem() {
        lineDialogue = new LineDialog(frame);
        lineDialogue.setVisible(true);
    }

    private void deleteItem() {
        int selectedRow = frame.getLineTable().getSelectedRow();

        if (selectedRow != -1) {
            LineTableModel linesTableModel = (LineTableModel) frame.getLineTable().getModel();
            linesTableModel.getLines().remove(selectedRow);
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }
    
    private void createInvoiceOK() {
        String date = invoiceDialogue.getInvDateField().getText();
        String customer = invoiceDialogue.getCustNameField().getText();
        int num = frame.getNextInvoiceNum();

        Invoice invoice = new Invoice(num, date, customer);
        frame.getInvoices().add(invoice);
        frame.getInvoicesTableModel().fireTableDataChanged();
        invoiceDialogue.setVisible(false);
        invoiceDialogue.dispose();
        invoiceDialogue = null;
    }

    private void createInvoiceCancel() {
        invoiceDialogue.setVisible(false);
        invoiceDialogue.dispose();
        invoiceDialogue = null;
    }    

    private void createLineOK() {
        String item = lineDialogue.getItemNameField().getText();
        String countStr = lineDialogue.getItemCountField().getText();
        String priceStr = lineDialogue.getItemPriceField().getText();
        int count = Integer.parseInt(countStr);
        double price = Double.parseDouble(priceStr);
        int selectedInvoice = frame.getInvoiceTable().getSelectedRow();
        if (selectedInvoice != -1) {
            Invoice invoice = frame.getInvoices().get(selectedInvoice);
            Line line = new Line(item, price, count, invoice);
            invoice.getLines().add(line);
            LineTableModel linesTableModel = (LineTableModel) frame.getLineTable().getModel();
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
        lineDialogue.setVisible(false);
        lineDialogue.dispose();
        lineDialogue = null;
    }

    private void createLineCancel() {
        lineDialogue.setVisible(false);
        lineDialogue.dispose();
        lineDialogue = null;
    }
    
    private void displayInvoices() {
        for (Invoice header : frame.getInvoices()) {
            System.out.println(header);
        }
    }
}
