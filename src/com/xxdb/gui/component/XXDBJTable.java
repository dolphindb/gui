package com.xxdb.gui.component;

import java.awt.Component;
import java.math.BigDecimal;
import java.text.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.*;
import javax.swing.event.*;

import com.xxdb.data.AbstractScalar;
import com.xxdb.data.AbstractVector;
import com.xxdb.data.Entity.DATA_TYPE;
import com.xxdb.data.Scalar;

import jxl.*;
import jxl.write.*;
import org.jfree.ui.NumberCellRenderer;

import java.awt.datatransfer.*;
import java.awt.Toolkit;

public class XXDBJTable extends JTable implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private TableSelectionListener listener;
	private TablePageSwitchListener pageListener;
	protected TableCellRenderer[] renderers;
	private TableSelectionModel tableSelectionModel;
	private boolean cellSelected;
	private MouseDoubleClickListener mcl;
	private TableToolTipHandler tipHandler;
	private final JMenuItem mnuFirstPage = new JMenuItem("First Page");
	private final JMenuItem mnuPrevPage = new JMenuItem("Prev Page");  
	private final JMenuItem mnuNextPage = new JMenuItem("Next Page"); 
	private final JMenuItem mnuLastPage = new JMenuItem("Last Page");    
	private final JMenuItem mnuAutoResize = new JMenuItem("Auto Resize");   
	
	public XXDBJTable(AbstractXXDBTableModel model,TableSelectionListener listener){
		this(model,listener,null);
	}
		
	public XXDBJTable(AbstractXXDBTableModel model,TableSelectionListener listener, TablePageSwitchListener pageListener){
		super(model);
		mcl = null;
		tipHandler = null;
		getTableHeader().setReorderingAllowed(model.isColumnReorderingAllowed());
		if(model.isRowReorderingAllowed())
			setRowSorter(new TableRowSorter<>(model));
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		adjustWidthAndHeight();

		this.listener=listener;
		this.pageListener = pageListener;
		
		getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				handleSelection(event);
		    }
		});
			
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				handleSelection(event);
		    }
		});
		this.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(java.awt.event.KeyEvent e){
				if(e.isControlDown()&&e.getKeyCode()==KeyEvent.VK_C){
					handleCopy();
				}
			}
		});
		
	    createDefaultTableSelectionModel();
	    setUI(new BasicTableSelectionUI(mcl));

	    super.setCellSelectionEnabled(true);
	    cellSelected = true;
	    
	    final JPopupMenu menu=new JPopupMenu();
	    
	    final JMenuItem mnuCopy = new JMenuItem("Copy");    
	    mnuCopy.addActionListener(this);
	    mnuCopy.setActionCommand("Copy");
	    menu.add(mnuCopy);


	    final JMenuItem mnuCopyAll = new JMenuItem("CopyAll");    
	    mnuCopyAll.addActionListener(this);
	    mnuCopyAll.setActionCommand("CopyAll");
	    menu.add(mnuCopyAll);
	    menu.addSeparator();
	    
    
	    final JMenuItem mnuPrint = new JMenuItem("Print...");    
	    mnuPrint.addActionListener(this);
	    mnuPrint.setActionCommand("Print");
	    menu.add(mnuPrint);
	    menu.addSeparator();
	    
	    final JMenuItem mnuSort = new JMenuItem("Sort...");    
	    mnuSort.addActionListener(this);
	    mnuSort.setActionCommand("Sort");
	    menu.add(mnuSort);
	    menu.addSeparator();
	    
	    final JMenuItem mnuFormat = new JMenuItem("Format...");    
	    mnuFormat.addActionListener(this);
	    mnuFormat.setActionCommand("Format");
	    menu.add(mnuFormat);
	    menu.addSeparator();
	    
	    final JMenuItem mnuExport = new JMenuItem("Export to Excel");    
	    mnuExport.addActionListener(this);
	    mnuExport.setActionCommand("Export");
	    menu.add(mnuExport);
	    menu.addSeparator();
	    
	    if(model.getPageCount()>1){
	    	mnuFirstPage.addActionListener(this);
	    	mnuFirstPage.setActionCommand("First");
		    menu.add(mnuFirstPage);
		    mnuFirstPage.setEnabled(false);
		    
	    	mnuPrevPage.addActionListener(this);
	    	mnuPrevPage.setActionCommand("Prev");
		    menu.add(mnuPrevPage);
		    mnuPrevPage.setEnabled(false);
		       
	    	mnuNextPage.addActionListener(this);
	    	mnuNextPage.setActionCommand("Next");
		    menu.add(mnuNextPage);
		      
	    	mnuLastPage.addActionListener(this);
	    	mnuLastPage.setActionCommand("Last");
		    menu.add(mnuLastPage);
		    menu.addSeparator();
	    }
	    
	    mnuAutoResize.addActionListener(this);
	    mnuAutoResize.setActionCommand("AutoResize");
	    menu.add(mnuAutoResize);
	   
	    if(pageListener != null)
	    	pageListener.handlePageSwitch(0, model.getPageCount());
	    if(model.showPopupMenu())
	    	addMouseListener(new PopupListener(menu));
	}
	
	public void setToolTipHandler(TableToolTipHandler tipHandler){
		this.tipHandler=tipHandler;
	}
	
	public void setTableSelectionListener(TableSelectionListener listener){
		this.listener = listener;
	}
	
	public void setPageSwitchListener(TablePageSwitchListener listener){
		this.pageListener = listener;
	}
	
	public void actionPerformed(ActionEvent e){
		String action;
		
		action=e.getActionCommand();
		if(action.equalsIgnoreCase("Print")){
			handlePrint();
		}
		else if(action.equalsIgnoreCase("Sort")){
			handleSort();
		}
		else if(action.equalsIgnoreCase("Format")){
			handleFormat();
		}
		else if(action.equalsIgnoreCase("Export")){
			handleExport();
		}
		else if(action.equalsIgnoreCase("Copy")){
			handleCopy();
		}
		else if(action.equalsIgnoreCase("CopyAll")){
			handleCopyAll();
		}
		else if(action.equalsIgnoreCase("First")){
			handleFirstPage();
		}
		else if(action.equalsIgnoreCase("Last")){
			handleLastPage();
		}
		else if(action.equalsIgnoreCase("Prev")){
			handlePrevPage();
		}
		else if(action.equalsIgnoreCase("Next")){
			handleNextPage();
		}
		else if(action.equalsIgnoreCase("AutoResize")){
			handleTableResizeMode(true);
		}
		else if(action.equalsIgnoreCase("ManualResize")){
			handleTableResizeMode(false);
		}
	}
	
	public String getToolTipText(MouseEvent event) {
		if(tipHandler==null) 
			return super.getToolTipText(event);
		else
			return tipHandler.getToolTipText(event);
	}
	
	public void addTableSelectionListener(TableSelectionListener listener){
		this.listener = listener;
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				handleSelection(event);
		    }
		});
	}
	
	public boolean cellSelectionMode(){
		return this.cellSelected;
	}

	public void setColumnSelectionAllowed(boolean colSelect){
		this.cellSelected = colSelect;
		super.setColumnSelectionAllowed(colSelect);
	}
	
	
	@SuppressWarnings("unchecked")
	public void updateUI(){
		TableRowSorter<AbstractXXDBTableModel> sorter;
		List<? extends SortKey> sortKeys;
		TableColumnModel cm;
		TableColumn column;
		AbstractXXDBTableModel model;
		int i;
		
		//update table column model if necessary
		adjustColumnModel();
		
		//update column name
		model=(AbstractXXDBTableModel)this.getModel();
		cm =getColumnModel();
		for(i=0; i<cm.getColumnCount(); i++){
			column=cm.getColumn(i);
			column.setHeaderValue(model.getColumnName(column.getModelIndex()));
		}
		
		//update number and date format
		updateDataFormat();
		
		//update sorter and filter
		sorter=(TableRowSorter<AbstractXXDBTableModel>)getRowSorter();
		if(sorter!=null){
			sortKeys=sorter.getSortKeys();
			sorter.setModel((AbstractXXDBTableModel)getModel());
			sorter.setSortKeys(sortKeys);
			if(this.getRowCount()==0 && this.getModel().getRowCount()>0){
				sorter.sort();
			}
		}
		
		//update table graphical interface
		setUI(new BasicTableSelectionUI(mcl));
	}
	
	public boolean isCellSelected(int row, int column) {
		if (cellSelected)
			if (tableSelectionModel == null)
				return false;
			else
			  return tableSelectionModel.isSelected(row, convertColumnIndexToModel(column));
		else 
			return super.isCellSelected(row, column);
	}
	
	private void createDefaultTableSelectionModel() {
		TableSelectionModel tsm;
		 tsm = new TableSelectionModel(this);
		 setTableSelectionModel(tsm);
	}


	public void setTableSelectionModel(TableSelectionModel newModel) {
		if (newModel == null) {
			throw new IllegalArgumentException("Cannot set a null TableSelectionModel");
		}
		
		//save the old Model
		TableSelectionModel oldModel = this.tableSelectionModel;
		//set the new Model
		this.tableSelectionModel = newModel;
		//The model needs to know how many columns are there
		//newModel.setColumns(getColumnModel().getColumnCount());
		newModel.setColumns(getModel().getColumnCount());
		getModel().addTableModelListener(newModel);
		
		if (oldModel != null) {
		  removePropertyChangeListener(oldModel);
		}
		addPropertyChangeListener(newModel);
		
		firePropertyChange("tableSelectionModel", oldModel, newModel);
	}

	public TableSelectionModel getTableSelectionModel() {
		 return tableSelectionModel;
	}
	
	public double getSelectionSum(){
		int i, j, col,row, cols[], rows[];
		double sum = 0;
		
		rows=getSelectedRows();
		cols=getSelectedColumns();
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
		try{
			for(j=0;j<cols.length;j++){
				col=cols[j];
				int modelCol = convertColumnIndexToModel(col);
				if(!model.getColumnMeta(modelCol).isNumber())
					continue;
				for(i=0;i<rows.length;i++){
					row=rows[i];
					if(this.isCellSelected(row, col)){
						Scalar obj=(Scalar)getValueAt(row, col);
						sum=sum+(obj==null?0:obj.getNumber().doubleValue());
					}
				}
			}
		}
		catch(Exception ex){
			
		}
		return sum;
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableCellRenderer renderer;
		
		renderer=renderers==null?null:renderers[convertColumnIndexToModel(column)];
		if(renderer!=null)
			return renderer;
		else
			return super.getCellRenderer(row, column);
    }
	
	public void setCellRenderer(int modelCol, TableCellRenderer cr) {
        if(renderers==null)
        	renderers=new TableCellRenderer[getModel().getColumnCount()];
        
        renderers[modelCol]=cr;
    }
	
	public void adjustWidthAndHeight(){
		int colNum;
		
		this.updateUI();
		
		colNum=this.getColumnModel().getColumnCount();
		int height = 0;
		for(int i = 0;i<colNum;i++){
			TableColumn col = this.getColumnModel().getColumn(i);
			TableCellRenderer render = col.getHeaderRenderer();
			if(render == null)
				render = this.getTableHeader().getDefaultRenderer();
			Component comp = render.getTableCellRendererComponent(this, col.getHeaderValue(), false, false, 0, 0);
			int width = comp.getPreferredSize().width;
			
			int testRows = Math.min(getRowCount(), 2);
			for(int j =0;j<testRows;j++){
				render = this.getCellRenderer(j, i);
				comp = render.getTableCellRendererComponent(this,this.getValueAt(j, i), false, false, j, i);
				width = Math.max(width,comp.getPreferredSize().width);
				height = Math.max(height, comp.getPreferredSize().height);
			}
			width += 2*3;
			col.setPreferredWidth(Math.max(50, Math.min(width,200)));
		}
		if(height > 10)
			this.setRowHeight(height);
	}
	
	private void updateDataFormat(){
		TableCellRenderer renderer;
		
		AbstractXXDBTableModel model=(AbstractXXDBTableModel)this.getModel();
		if(renderers==null ||renderers.length!=model.getColumnCount())
			renderers= new TableCellRenderer[model.getColumnCount()];
		for(int i=0;i<renderers.length;i++){
			renderer=renderers[i];
			TableColumnMeta meta = model.getColumnMeta(i);
			if(meta.isNumber()){
				if(renderer==null)
					renderers[i]=new NumberCellRenderer(meta.getFormat());
				else if(renderer instanceof NumberCellRenderer)
					((NumberCellRenderer)renderer).setFormat(meta.getFormat());
			}
			else if(meta.isTemporal()) {// && meta.isFormatSet()){
				if (meta.isFormatSet()) {
					if(renderer==null)
						renderers[i]=new TemporalCellRenderer(meta.getFormat());
					else if(renderer instanceof TemporalCellRenderer)
						((TemporalCellRenderer)renderer).setFormat(meta.getFormat());
				}
				else {
					renderers[i]=new TemporalCellRenderer();
					meta.setFormat(((TemporalCellRenderer)renderers[i]).getFormat());
				}
			}
			else if(meta.isTuple()){
				if(renderer==null)
					renderers[i]=new TupleCellRenderer();
			}
		}
	}
	
	private void adjustColumnModel(){
		AbstractXXDBTableModel model; 
		TableColumnModel columnModel;
		TableColumn column;
		boolean[] arrVisible;
		int i, changeType;
		
		model=(AbstractXXDBTableModel)getModel(); 
		columnModel = getColumnModel();
		changeType=0;
		
		arrVisible= new boolean[model.getColumnCount()];
		for(i=0;i<columnModel.getColumnCount(); i++){
			column=columnModel.getColumn(i);
			if(column!=null){
				if(column.getModelIndex()>=arrVisible.length){
					changeType=2;
					break;
				}
				else
					arrVisible[column.getModelIndex()]=true;
			}
		}
		
		if(changeType==0){
			for(i=0;i<arrVisible.length;i++){
				if(!arrVisible[i] ){
					changeType=2;
					break;
				}
			}
		}

		if(changeType==0)
			return;
		
		if(changeType==2)
			createDefaultColumnsFromModel();
        for(i=0; i<columnModel.getColumnCount(); i++){
        	column=columnModel.getColumn(i);
        	if(column==null)
        		continue;
        }
	}

	private void handlePrint(){
		MessageFormat footerFormat;
		
		try {
			footerFormat = new MessageFormat("- {0} -");
			print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
        } 
		catch (Exception pe) {
        	JOptionPane.showMessageDialog(this,"Error printing: " + pe.getMessage());
        }
	}
	
	private void handleSort(){
		try{
			BasicTableSortSetupDlg dlg=new BasicTableSortSetupDlg(this);
			dlg.setVisible(true);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this,"Error occurred during sorting: " + e.getMessage());
		}
	}
	
	private void handleFormat(){
		try{
			BasicTableFormatSetupDlg dlg=new BasicTableFormatSetupDlg(this);
			dlg.setVisible(true);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this,"Error occurred during formating: " + e.getMessage());
		}
	}
	
	private void handleExport(){
		AbstractXXDBTableModel model;
		WritableCellFormat format;
		List<Integer>colList;
		File xls;
		int rowNumber, colNumber, mCol, mRow;
		int i,j;
		
		rowNumber = getRowCount();
		colNumber = getColumnCount();
		if(rowNumber==0) {
			JOptionPane.showMessageDialog(this, "Nothing to be exported.");
			return;
		}
		
		try{
			xls = File.createTempFile("tmp_table_export_", ".xls");
			if(!xls.exists()){
				JOptionPane.showMessageDialog(this, "Could't open tmp file");
				return;
			}
			
			WritableWorkbook workbook = Workbook.createWorkbook(xls);
			WritableSheet sheet = workbook.createSheet("Sheet1", 0);
			  
			colList=new ArrayList<Integer>();
			for(j =0; j<colNumber; j++){
				sheet.addCell(new Label(j,0,getColumnName(j)));
				colList.add(j);
			}

			model=(AbstractXXDBTableModel)getModel();
			for(j=0; j<colList.size(); j++){	
				mCol=this.convertColumnIndexToModel(colList.get(j));
				
				format=null;
				if (model.getColumnMeta(mCol).isNumber()){
					if(model.getNumberFormat(mCol)!=null)
						format=new WritableCellFormat(new jxl.write.NumberFormat(model.getNumberFormat(mCol)));
					else {
						if(model.getColumnMeta(mCol).isInteger()) {
							if("com.xxdb.data.BasicByte"!=model.getColumnMeta(mCol).getObjectClass().getName()){
								format = new WritableCellFormat(new jxl.write.NumberFormat("0"));
							}
						}
						else
							format=new WritableCellFormat(new jxl.write.NumberFormat("#,##0.####;(#,##0.####)"));
					}

				}	
				
				for(i=0; i<rowNumber; i++){
					mRow=this.convertRowIndexToModel(i);
					if(model.getValueAt(mRow,mCol)==null)
						sheet.addCell(new Label(j,i+1,""));
					else if(format!=null){
						Scalar obj = (Scalar)model.getValueAt(mRow, mCol);
						if(obj.isNull())
							sheet.addCell(new Label(j,i+1,""));
						else
							sheet.addCell(new jxl.write.Number(j,i+1,obj.getNumber().doubleValue(),format));
					}
					else
						sheet.addCell(new Label(j,i+1,model.getValueAt(mRow, mCol).toString()));
				}
			}
		
			workbook.write();
			workbook.close();
			
			Runtime.getRuntime().exec(new String[]{ "rundll32", "url.dll,FileProtocolHandler", xls.getAbsolutePath() });
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void handleCopy() {
		Clipboard sysClipBoard=Toolkit.getDefaultToolkit().getSystemClipboard();
		
		try {
            StringSelection stsel = null;
            int selectedRow = getSelectedRow(), selectedColumn = getSelectedColumn();
	        Format arrFormat = null;

	        AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
	        if (model.getColumnMeta(selectedColumn).isNumber()) {
	        	if (model.getColumnMeta(selectedColumn).isFormatSet())
	        		arrFormat = new DecimalFormat(model.getNumberFormat(selectedColumn));
	        	else
	        		arrFormat = new DecimalFormat("#,##0.####;(#,##0.####)");
	        }
			Object obj = getValueAt(selectedRow, selectedColumn);
			
			if (obj == null);
			else if (arrFormat != null) {
				if (!((Scalar)obj).isNull()){
					BigDecimal bigDecimal = new BigDecimal(((Scalar)obj).getNumber().toString());
					stsel = new StringSelection(arrFormat.format(bigDecimal));
				}
			}
			else if (obj instanceof AbstractVector){
				stsel = new StringSelection(((AbstractVector)obj).getString());
			}
			else
				stsel = new StringSelection(obj.toString());
			
			sysClipBoard.setContents(stsel, stsel);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			sysClipBoard.setContents(new StringSelection(null),new StringSelection(null));
			e.printStackTrace();
		}
	}

	private void handleCopyAll(){
        Clipboard sysClipBoard=Toolkit.getDefaultToolkit().getSystemClipboard();
		
        try{
            StringSelection stsel;
            StringBuffer sb=new StringBuffer();
            int numCols = getColumnCount();
            int numRows = getRowCount();
            
	        Format[] arrFormat = new Format[numCols];
	        AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
	        for (int i=0;i<numCols;i++){
	        	final int mCol= convertColumnIndexToModel(i);
	        	if(model.getColumnMeta(mCol).isNumber()){
	        		if(model.getColumnMeta(mCol).isFormatSet())
						arrFormat[i] = new DecimalFormat(model.getNumberFormat(mCol));
					else
						arrFormat[i] = new DecimalFormat("#,##0.####;(#,##0.####)");
	        	}
	        	sb.append(model.getColumnName(mCol));
	        	if(i<numCols-1)
	        		sb.append("\t");
	        }
	        
	        sb.append("\n");
	        for (int i=0;i<numRows;i++){
	        	for (int j=0;j<numCols;j++){
	        		Object obj = getValueAt(i, j);
	        		
	        		if(obj==null)
						;
					else if(arrFormat[j]!=null){
						if(!((Scalar)obj).isNull())
							sb.append(arrFormat[j].format(((Scalar)obj).getNumber().doubleValue()));
					}
					else
						sb.append(obj.toString());
	        		
	        	   if (j<numCols-1) sb.append("\t");
	           }
	           sb.append("\n");
	        }
	        
	        stsel  = new StringSelection(sb.toString());
	        sysClipBoard.setContents(stsel,stsel);
        }
        catch(Exception e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			sysClipBoard.setContents(new StringSelection(null),new StringSelection(null));
			e.printStackTrace();
		}
		
	}
	
	public void moveToPage(int pageIndex){
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
		if(model.getCurrentPage()==pageIndex)
			return;
		try{
			model.moveToPage(pageIndex);
		}
		catch(Throwable t){
			JOptionPane.showMessageDialog(null, t.getMessage());
			return;
		}
		
		updateUI();
		if(pageListener!=null)
			pageListener.handlePageSwitch(model.getCurrentPage(), model.getPageCount());
		int pageCount = model.getPageCount();
		mnuFirstPage.setEnabled(pageIndex>0);
		mnuPrevPage.setEnabled(pageIndex>0);
		mnuNextPage.setEnabled(pageIndex<pageCount-1);
		mnuLastPage.setEnabled(pageIndex<pageCount-1);
	}
	
	protected void handleSelection(ListSelectionEvent event){
		if(listener != null)
			listener.handleTableSelection(event, this);
	}
	
	private void handleFirstPage(){
		moveToPage(0);
	}
	
	private void handleLastPage(){
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
		moveToPage(model.getPageCount() - 1);
	}
	
	private void handlePrevPage(){
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
		moveToPage(model.getCurrentPage() - 1);
	}
	
	private void handleNextPage(){
		AbstractXXDBTableModel model = (AbstractXXDBTableModel)getModel();
		moveToPage(model.getCurrentPage() + 1);
	}
	
	private void handleTableResizeMode(boolean auto){
		if(auto){
			setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			mnuAutoResize.setActionCommand("ManualResize");
			mnuAutoResize.setText("Manual Resize");
		}
		else{
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			mnuAutoResize.setActionCommand("AutoResize");
			mnuAutoResize.setText("Auto Resize");
		}
	}
	
	private static class NumberCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private NumberFormat format;
        private String strFormat;
		
        public NumberCellRenderer(String numFormat) {
        	if(numFormat==null){
        			numFormat= getDeafultFormat();
        	}
        	this.strFormat=numFormat;
            format=new DecimalFormat(numFormat);
        }
        
		private String getDeafultFormat(){
			int f = PreferenceSettingUtil.getDisplay_DecimalPlace();
			if(f>0 && f<=32){
				String sf = "################################".substring(0, f);
				return String.format("#,##0.%s;(#,##0.%s)", sf,sf);
			}else{
				return "#,##0.####;(#,##0.####)";
			}
		}
	
        public void setFormat(String numFormat){
        	if(numFormat==null)
        		numFormat= getDeafultFormat();
        	if(!numFormat.equals(strFormat))
        		format=new DecimalFormat(numFormat);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            if(value==null || value.toString().equals(""))
            	setText("");
            else {
            	try{
            		Scalar val = (Scalar)value;
            		if (val.getDataType() == DATA_TYPE.DT_BYTE) {
            			setText(((Scalar)value).getString());
            		}
            		else{
						if (val.getString().contains("-")){
							setText(val.getString());
						}else {
							setText(format.format(val.getNumber()));
						}
					}
            	}
            	catch(Exception ex){
            		setText("");
            	}
            }
            return this;
        }
    }

	public static  TableToolTipHandler getDefaultToolTipHandler(final XXDBJTable table){
		return new TableToolTipHandler(){
			public String getToolTipText(MouseEvent e){
		        java.awt.Point p = e.getPoint();
		        int rowIndex = table.rowAtPoint(p);
		        return "Line "+(rowIndex+1);
			}
		};
	}
	
	private static class TemporalCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private DateTimeFormatter format;
		private String strFormat;
        	
        public TemporalCellRenderer(String dateFormat) {
        	if(dateFormat==null || dateFormat.isEmpty())
        		throw new IllegalArgumentException("The temporal format can't be empty.");
        	format = DateTimeFormatter.ofPattern(dateFormat);
        	this.strFormat=dateFormat;
        }
        
        public TemporalCellRenderer() {
        	this.strFormat = "";
        }
        
        public void setFormat(String dateFormat){
        	if(dateFormat==null || dateFormat.isEmpty())
        		throw new IllegalArgumentException("The temporal format can't be empty.");
        	if(!dateFormat.equals(strFormat)){
        		format = DateTimeFormatter.ofPattern(dateFormat);
            	this.strFormat=dateFormat;
        	}
        }
        
        public String getFormat() {
        	return strFormat;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            if(value==null)
            	setText("");
            else if(strFormat.isEmpty())
            	setText(((Scalar)value).getString());
            else{
            	try{
            	setText(format.format(((Scalar)value).getTemporal()));
            	}
            	catch(Exception ex){
            		setText("");
            	}
            }
            return this;
        }
    }
	private static class TupleCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			if(value instanceof AbstractVector){
				setText(((AbstractVector)value).getString());
			}else if(value instanceof AbstractScalar){
				setText(((AbstractScalar)value).getString());
			}
			return this;
		}
	}
	public static class PopupListener extends MouseAdapter { 
		private JPopupMenu menu;
		
		public PopupListener(JPopupMenu menu){
			this.menu=menu;
		}
		public void mousePressed(MouseEvent e) { 
         	showPopup(e); 
		} 
	     
		public void mouseReleased(MouseEvent e) { 
		    showPopup(e); 
		} 
		    
		private void showPopup(MouseEvent e) {  
			if (e.isPopupTrigger()) {         
				menu.show(e.getComponent(), e.getX(), e.getY());     
			} 
		} 
	} 
	
}
