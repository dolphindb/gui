package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicStringVector;
import com.xxdb.data.BasicTable;
import com.xxdb.data.Entity;
import com.xxdb.data.Entity.DATA_FORM;
import com.xxdb.data.Scalar;
import com.xxdb.gui.common.ListItem;
import com.xxdb.gui.common.Utility;
import com.xxdb.gui.data.Server;
import com.xxdb.streaming.client.IMessage;
import com.xxdb.streaming.client.PollingClient;
import com.xxdb.streaming.client.TopicPoller;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.data.time.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class RealTimeDataChartDlg extends JFrame implements Runnable, ActionListener {
	private static final long serialVersionUID = 1L;

	XXDBCollapsiblePanel topPanel = null;
	PollingClient client = null;
	TopicPoller poller1 = null;
	ConfigPanel configPane = null;
	TimeSeriesCollection timeseriescollection = null;
	int randomPort = getFreePort();
	static boolean  stopThread = true;

	int pivotFieldIndex = 0;
	HashSet<Entity> pivotSeries = new LinkedHashSet<Entity>();

	private XXDBEditor editor = null;
	private JFreeChart chart = null;

	private TimeTableXYDataset tablexydataset = new TimeTableXYDataset();
	boolean isPivot = true;
	
	String currentActionNameId = "";
	
	public int getRandom(int min, int max) {
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		return s;
	}
	
	private int getFreePort() {
		int p = 0;
		do {
			try {
				p = getRandom(50000, 65534);
				DatagramSocket ds = new DatagramSocket(p);
				ds.close();
				break;
			} catch (SocketException e) {

			}
		} while (true);
		
		return p;
	}
	
	public RealTimeDataChartDlg(XXDBEditor main) {
		editor = main;
		try {
			client = new PollingClient(randomPort);
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		this.setLocationRelativeTo(null);
		this.setIconImage(new ImageIcon(XXDBDataBrowser.class.getResource("/logo.jpg")).getImage());

		setResizable(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Realtime Plotting");
		setSize((int) (1000), (int) (700));
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(4, 4));

		topPanel = new XXDBCollapsiblePanel();
		topPanel.setTitle("Plotting Configuration");

		getContentPane().add(BorderLayout.NORTH, topPanel);

		configPane = new ConfigPanel();
		configPane.btnPlot.addActionListener(this);
		configPane.btnStopPlot.addActionListener(this);
		topPanel.add(configPane);

		timeseriescollection = new TimeSeriesCollection();
		//just for vi effect
		chart = ChartFactory.createTimeSeriesChart("", "", "", null, true, true, false);
	
		JPanel contentPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		Border b1 = BorderFactory.createEmptyBorder(10, 20, 10, 20);
		Border b3 = BorderFactory.createTitledBorder("");
		contentPanel.setBorder(BorderFactory.createCompoundBorder(b1, b3));
		ChartPanel chartFrame = new ChartPanel(chart);
		chartFrame.setBackground(Color.WHITE);
		contentPanel.add(chartFrame);
		getContentPane().add(BorderLayout.CENTER, contentPanel);
		// unsubscribe when dialog window close
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				if (client != null && stopThread == false) {
					try {
						client.unsubscribe(configPane.getHost(), configPane.getPort(), configPane.getTableName(),"realTimeChart_" + currentActionNameId);
					} catch (Exception e1) {
						e1.printStackTrace();
						dispose();
					}
				}
				dispose();
			}
		});
	}

	private JFreeChart CreateChart(boolean isStack) {
		this.chart = null;
		
		JFreeChart chart1;
		if (isStack) {
			chart1 = ChartFactory.createStackedXYAreaChart(configPane.getChartTitle(),configPane.getXaxisTitle(), configPane.getYaxisTitle(), tablexydataset, PlotOrientation.VERTICAL, true, true, false);
			 final StackedXYAreaRenderer render = new StackedXYAreaRenderer();
		        render.setSeriesPaint(0, Color.RED);
		        render.setSeriesPaint(1, Color.GREEN);

		        DateAxis domainAxis = new DateAxis();
		        domainAxis.setAutoRange(true);
				
		        XYPlot plot = (XYPlot) chart1.getPlot();
		        plot.setRenderer(render);
		        plot.setDomainAxis(domainAxis);
		        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		        plot.setForegroundAlpha(0.5f);
		} else {
			chart1 = ChartFactory.createTimeSeriesChart(configPane.getChartTitle(), configPane.getXaxisTitle(),
					configPane.getYaxisTitle(), timeseriescollection, true, true, false);
		    XYPlot plot = (XYPlot) chart1.getPlot();
		    plot.setForegroundAlpha(0.5f);
		    
		}
		
		JPanel contentPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		Border b1 = BorderFactory.createEmptyBorder(10, 20, 10, 20);
		Border b3 = BorderFactory.createTitledBorder("");
		contentPanel.setBorder(BorderFactory.createCompoundBorder(b1, b3));
		ChartPanel chartFrame = new ChartPanel(chart1);
		contentPanel.add(chartFrame);
		getContentPane().add(BorderLayout.CENTER, contentPanel);
		this.getContentPane().validate();
		this.getContentPane().repaint();
		return chart1;
	}



	private void addPointToChart(IMessage msg) {
		if (isPivot) {
			if(configPane.getIsStack()){
				addPivotStackPoint(msg);	
			} else {
				addPivotPoint(msg);
			}
		} else {
			addSeriesPoint(msg);
		}
	}

	private void addPivotPoint(IMessage msg){
		
		int temporalFieldIndex = configPane.getTemporalFieldName().getValue();
		Entity tf = msg.getValue(temporalFieldIndex); 
		pivotFieldIndex = configPane.getPivotFieldIndex();
		Entity pField = msg.getValue(pivotFieldIndex);
		pivotSeries.add(pField);
		
		int pivotValueFieldIndex = configPane.getPivotValueFieldIndex();
		ListItem titem = new ListItem(pivotValueFieldIndex, pField.toString(), pField.getDataType().toString());
		
		//1.create timeseries until 32 series get full if there is no value selected  
		//2.there are items been selected, check and append series which selected after plotting begin 
		List<ListItem> pivotCheckedItems = configPane.getPivotFieldListBox().getCheckedItems();
		TimeSeries tsItem = timeseriescollection.getSeries(pField.toString());
		if(tsItem==null){
			if(pivotCheckedItems.isEmpty()){
				 int seriesCount = timeseriescollection.getSeriesCount();
				 if(seriesCount<32){
					 timeseriescollection.addSeries(new TimeSeries(pField.toString(), Millisecond.class));
					 tsItem = timeseriescollection.getSeries(pField.toString());
				 }else{
					 return;
				 }
			}
			else {
				
				if(pivotCheckedItems.contains(titem)){
					timeseriescollection.addSeries(new TimeSeries(pField.toString(), Millisecond.class));
					tsItem = timeseriescollection.getSeries(pField.toString());
				}
				else{
					return;
				}
			}
		}
		 
		Entity eVal = msg.getEntity(titem.getValue());
		if(eVal.getDataForm() == DATA_FORM.DF_SCALAR){
			
			Number val = null;
			try {
				val = ((Scalar)eVal).getNumber();
			} catch (Exception e) {
				e.printStackTrace();
			}
			RegularTimePeriod period = getUtilDateForTimeSeries(tf.toString());
			tsItem.addOrUpdate(period,val);
		}
		
	}
	
	
	private void addPivotStackPoint(IMessage msg) {
		int temporalFieldIndex = configPane.getTemporalFieldName().getValue();
		Entity tf = msg.getValue(temporalFieldIndex);
		pivotFieldIndex = configPane.getPivotFieldIndex();
		Entity pField = msg.getValue(pivotFieldIndex);

		int pivotValueFieldIndex = configPane.getPivotValueFieldIndex();
		ListItem titem = new ListItem(pivotValueFieldIndex, pField.toString(), pField.getDataType().toString());
		List<ListItem> pivotCheckedItems = configPane.getPivotFieldListBox().getCheckedItems();
		boolean isUserChecked = !pivotCheckedItems.isEmpty();

		if (isUserChecked) {
			if (!pivotCheckedItems.contains(titem)) {
				return;
			}
		} else {

			if (pivotSeries.size() >= 32) {
				boolean isExists = false;
				Iterator<Entity> iter = pivotSeries.iterator();
				do {
					Entity e = iter.next();
					if (e.toString().equals(pField.toString())) {
						isExists = true;
					}
				} while (iter.hasNext());
				if (!isExists) {
					return;
				}
			}
		}

		pivotSeries.add(pField);
	
		Entity eVal = msg.getEntity(titem.getValue());
		if (eVal.getDataForm() == DATA_FORM.DF_SCALAR) {

			Number val = null;
			try {
				val = ((Scalar) eVal).getNumber();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			RegularTimePeriod period = getUtilDateForTimeSeries(tf.toString());
			tablexydataset.add((TimePeriod) period, val.doubleValue(), pField.toString());
		}

	}

	private void addSeriesPoint(IMessage msg) {
		int temporalFieldIndex = configPane.getTemporalFieldName().getValue();
		Entity tf = msg.getValue(temporalFieldIndex);
		for (ListItem field : configPane.getSeriesFields()) {
			TimeSeries tsItem = timeseriescollection.getSeries(field.getText());
			Entity eVal = msg.getEntity(field.getValue());
			if (eVal.getDataForm() == DATA_FORM.DF_SCALAR) {

				Number val = null;
				try {
					val = ((Scalar) eVal).getNumber();
				} catch (Exception e) {
					e.printStackTrace();
				}
				RegularTimePeriod period = getUtilDateForTimeSeries(tf.toString());
				tsItem.addOrUpdate(period, val);
			}
		}

	}

	@Override
	public void run() {

		isPivot = configPane.chkIsPivot.isSelected();
		this.chart = CreateChart(configPane.getIsStack());
		DateAxis axis = (DateAxis)this.chart.getXYPlot().getDomainAxis();
		int fixedRange = configPane.txtFixRange.getText().isEmpty() ? -1
				: Integer.valueOf(configPane.txtFixRange.getText());
	
		if (fixedRange > 0){
			axis.setFixedAutoRange(fixedRange * 60 * 1000D);
		} else {
			axis.setFixedAutoRange(0);		
		}
		// add static series fields
		timeseriescollection.removeAllSeries();
		if (!isPivot) {
			List<ListItem> seriesFields = configPane.getSeriesFields();
			for (ListItem item : seriesFields) {
				TimeSeries ts = new TimeSeries(item.getText(), Millisecond.class);
				timeseriescollection.addSeries(ts);
			}
		} else {
			List<ListItem> lstChecked = configPane.getPivotFieldListBox().getCheckedItems();
			for (ListItem fs : lstChecked) {
				TimeSeries ts1 = new TimeSeries(fs.getText(), Millisecond.class);
				timeseriescollection.addSeries(ts1);
			}
		}

		while (true) {
			try {
				if (stopThread == true) {
					break;
				}
				if(poller1!=null){
					ArrayList<IMessage> msgs = poller1.poll(100);
					
					if (msgs == null)
						continue;
					for (IMessage msg : msgs) {
						addPointToChart(msg);
						Thread.sleep(100);
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean checkConfig() {
		if (this.configPane.getHost().isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Select an existing server or type the streaming server in the format of <host>:<port> e.g. localhost:8848");
			return false;
		}
		if (this.configPane.getPort() < 0) {
			JOptionPane.showMessageDialog(this,
					"Select an existing server or type the streaming server in the format of <host>:<port> e.g. localhost:8848");
			return false;
		}
		if (this.configPane.getTableName().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select a stream table to subscribe");
			return false;
		}
		if (this.configPane.getTemporalFieldIndex() < 0) {
			JOptionPane.showMessageDialog(this, "Please choose a temporal field");
			return false;
		}
		if (this.configPane.getIsPivot()) {
			if (this.configPane.getPivotFieldIndex() < 0) {
				JOptionPane.showMessageDialog(this, "Please choose a pivot field");
				return false;
			}
			if (this.configPane.getPivotValueFieldIndex() < 0) {
				JOptionPane.showMessageDialog(this, "Please choose a value field");
				return false;
			}
			if (this.configPane.getTemporalFieldIndex() == this.configPane.getPivotFieldIndex()) {
				JOptionPane.showMessageDialog(this,
						"Please choose different table columns for temporal field and pivot field");
				return false;
			}
		} else {
			if (this.configPane.getSeriesFields().size() <= 0) {
				JOptionPane.showMessageDialog(this, "Please select chart series to plot");
				return false;
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == configPane.btnPlot) {
			JButton btn = (JButton) e.getSource();
			if (btn.getText().equalsIgnoreCase("Start Plotting")) {
				if (checkConfig() == false)
					return;
				stopThread = false;
				currentActionNameId = String.valueOf(new Date().getTime());
				try {
					poller1 = client.subscribe(configPane.getHost(), configPane.getPort(), configPane.getTableName(), "realTimeChart_" + currentActionNameId,
							configPane.getOffset());
					btn.setText("Stop Plotting");
					new Thread(this).start();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage());
					e1.printStackTrace();
				}
				
			} else if (btn.getText().equalsIgnoreCase("Stop Plotting")) {
				stopThread = true;
				try {
					client.unsubscribe(configPane.getHost(), configPane.getPort(), configPane.getTableName(),"realTimeChart_" + currentActionNameId);
					btn.setText("Start Plotting");
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage());
					e1.printStackTrace();
				}
			}

		}
	}

	private final String[] day_formats = { "yyyyMMdd", "yyyy.MM.dd", "yyyy/MM/dd", "yyyy-MM-dd", };
	private final String[] minute_formats = { "HH:mm" };
	private final String[] second_formats = { "HH:mm:ss", "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss",
			"MM/dd/yyyy HH:mm:ss", "yyyy.MM.dd'T'HH:mm:ss'Z'", "yyyy.MM.dd'T'HH:mm:ssZ", "yyyy.MM.dd'T'HH:mm:ss",
			"yyyy:MM:dd HH:mm:ss", };
	private final String[] millisecond_formats = { "HH:mm:ss.SSS", "HH:mm:ss.SSSSSSSSS", "HH:mm:ss.SSSSSSSSS'Z'",
			"HH:mm:ss.SSSSSSSSSZ", "yyyy.MM.dd'T'HH:mm:ss.SSS'Z'", "yyyy.MM.dd'T'HH:mm:ss.SSSZ",
			"yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSS'Z'", "yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSS", "yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSSZ",
			"MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS" };

	public RegularTimePeriod getUtilDateForTimeSeries(String dt) {
		for (String parse : millisecond_formats) {
			SimpleDateFormat sdf = new SimpleDateFormat(parse);
			sdf.setLenient(false);
			try {
				Date d = sdf.parse(dt);
				return new Millisecond(d);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}
		for (String parse : second_formats) {
			SimpleDateFormat sdf = new SimpleDateFormat(parse);
			sdf.setLenient(false);
			try {
				Date d = sdf.parse(dt);
				return new Second(d);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}
		for (String parse : minute_formats) {
			SimpleDateFormat sdf = new SimpleDateFormat(parse);
			sdf.setLenient(false);
			try {
				Date d = sdf.parse(dt);
				return new Minute(d);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}
		for (String parse : day_formats) {
			SimpleDateFormat sdf = new SimpleDateFormat(parse);
			sdf.setLenient(false);
			try {
				Date d = sdf.parse(dt);
				return new Day(d);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}
		return null;
	}

	public class ConfigPanel extends JPanel implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		JLabel lblDatanode = new javax.swing.JLabel("Stream Server");
		public JComboBox<String> cbServer = new JComboBox<String>();

		JLabel lblTableName = new javax.swing.JLabel("Stream Table");
		public JComboBox<String> cbTableName = new JComboBox<String>();
		JButton btnGetField = null;
		
		JLabel lblTemporal = new JLabel("Temporal Field");
		public JComboBox<ListItem> cbTemporalField = new JComboBox<ListItem>();

		JLabel lblSeriesField = new JLabel("Series");
		public XXDBJCheckListBox<ListItem> lstSeriesFields = new XXDBJCheckListBox<>();
		
		JLabel lblChartTitle = new JLabel("Chart Title");
		public JTextField txtChartTitle = new javax.swing.JTextField("");
		JLabel lblXaxisTitle = new JLabel("X Axis Title");
		public JTextField txtXaxisTitle = new javax.swing.JTextField("Time");
		JLabel lblYaxisTitle = new JLabel("Y Axis Title");
		public JTextField txtYaxisTitle = new javax.swing.JTextField("Value");
		
		public JButton btnPlot = new javax.swing.JButton("Start Plotting");
		public JButton btnStopPlot = new javax.swing.JButton("Stop Plotting");

		public JCheckBox chkIsPivot = new JCheckBox("Chart Series from Pivot Field");

		public JComboBox<ListItem> cbPivotField = new JComboBox<ListItem>();
		public XXDBJCheckListBox<ListItem> lstPivotFields = new XXDBJCheckListBox<>();

		public JComboBox<ListItem> cbPivotValueField = new JComboBox<ListItem>();

		public JCheckBox chkStack = new JCheckBox("Stack Chart Series");
		public JTextField txtFixRange = new JTextField();

		public JButton btnRefreshPivotValue = null;

		public JTextField txtPivotValue = new JTextField();

		JScrollPane jspSeriesFields = null;
		JScrollPane jspPivotFields = null;
		JPanel pnlFields = null;

		JButton btnGetTable = null;

		JComboBox<String> cbOffset = new JComboBox<String>();

		public String getHost() {
			String site = (String) cbServer.getSelectedItem();
			String[] svr = site.split(":");
			if (svr.length > 0)
				return svr[0];
			else
				return "";
		}

		public int getPort() {
			String site = (String) cbServer.getSelectedItem();
			String[] svr = site.split(":");
			if (svr.length > 1)
				return Integer.parseInt(svr[1]);
			else
				return 0;
		}

		public String getTableName() {
			if (cbTableName.getSelectedItem() == null) {
				return "";
			} else {
				return cbTableName.getSelectedItem().toString();
			}
		}

		public ListItem getTemporalFieldName() {
			return (ListItem) (cbTemporalField.getSelectedItem());
		}

		public int getTemporalFieldIndex() {
			ListItem item = (ListItem) cbTemporalField.getSelectedItem();
			if (item == null)
				return -1;
			else
				return item.getValue();
		}

		public List<ListItem> getSeriesFields() {
			return lstSeriesFields.getCheckedItems();
		}

		public String getChartTitle() {
			return txtChartTitle.getText();
		}

		public String getXaxisTitle() {
			return txtXaxisTitle.getText();
		}

		public String getYaxisTitle() {
			return txtYaxisTitle.getText();
		}

		public boolean getIsPivot() {
			return chkIsPivot.isSelected();
		}

		public boolean getIsStack(){
			return chkStack.isSelected();
		}
		public int getPivotFieldIndex() {
			ListItem item = (ListItem) cbPivotField.getSelectedItem();
			if (item == null)
				return -1;
			else
				return item.getValue();
		}

		public String getPivotFieldName() {
			ListItem item = (ListItem) cbPivotField.getSelectedItem();
			if (item == null)
				return "";
			else
				return item.getText();
		}

		public int getPivotValueFieldIndex() {
			ListItem item = (ListItem) cbPivotValueField.getSelectedItem();
			if (item == null)
				return -1;
			else
				return item.getValue();
		}

		public XXDBJCheckListBox<ListItem> getPivotFieldListBox() {
			return lstPivotFields;
		}

		private void getServerList() {
			Collection<Server> svrs = editor.sd.getServers();

			Iterator<Server> iter = svrs.iterator();
			do {
				Server svr = iter.next();
				cbServer.addItem(svr.getHost() + ":" + String.valueOf(svr.getPort()));
			} while (iter.hasNext());

		}

		public long getOffset() {

			String offset = (String) this.cbOffset.getSelectedItem();
			if (!offset.isEmpty()) {
				switch (offset) {
				case "from current":
					return -1;
				case "from beginning":
					return 0;
				default:
					return Long.parseLong(offset);
				}
			} else {
				return -1;
			}
		}

		private JButton createIconBtn(String imageUrl, String toolTipText) {
			ImageIcon image = new ImageIcon(XXDBEditor.class.getResource(imageUrl));
			image.setImage(image.getImage().getScaledInstance(Utility.getScaledSize(16), Utility.getScaledSize(16),
					Image.SCALE_DEFAULT));
			JButton icon = new JButton(image);
			icon.setPreferredSize(new Dimension(24, 24));
			icon.setToolTipText(toolTipText);
			return icon;
		}

		public ConfigPanel() {

			// init componnets
			cbOffset.setEditable(true);
			cbOffset.addItem("from current");
			cbOffset.addItem("from beginning");

			btnGetField = createIconBtn("/refresh.png", "Refresh Table Fields");
			btnGetField.addActionListener(this);

			btnRefreshPivotValue = createIconBtn("/refresh.png", "Refresh Pivot Values");
			btnRefreshPivotValue.setEnabled(false);
			btnRefreshPivotValue.addActionListener(this);

			btnGetTable = createIconBtn("/refresh.png", "Refresh Table List");
			btnGetTable.addActionListener(this);

			chkIsPivot.addActionListener(this);
			chkIsPivot.setActionCommand("PIVOT_CHECKED");

			txtPivotValue.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {

				}

				@Override
				public void keyReleased(KeyEvent e) {

				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == 10 && txtPivotValue.getText().isEmpty() == false) {
						ListItem item = new ListItem(0, txtPivotValue.getText(), "STRING");
						item.setSelected(true);

						TimeSeries ts1 = timeseriescollection.getSeries(item.getText());
						if (ts1 == null) {
							lstPivotFields.addItem(item);
							timeseriescollection.addSeries(new TimeSeries(item.getText(), Millisecond.class));
							txtPivotValue.setText("");
						}
					}

				}
			});

			getServerList();
			cbServer.setEditable(true);
			cbTableName.addActionListener(this);
			// set layout=========================

			setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
			JPanel container = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			container.setLayout(gridBagLayout);

			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.add(container);

			// Stream Server
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			lblDatanode.setPreferredSize(new Dimension(90, 25));
			p.add(lblDatanode);
			cbServer.setPreferredSize(new Dimension(210, 25));
			p.add(cbServer);
			container.add(p, new GBC(0, 0, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// Stream Table
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			lblTableName.setPreferredSize(new Dimension(90, 25));
			p.add(lblTableName);
			cbTableName.setPreferredSize(new Dimension(80, 25));
			p.add(cbTableName);
			cbOffset.setPreferredSize(new Dimension(100, 25));
			p.add(cbOffset);
			p.add(btnGetTable);
			container.add(p, new GBC(0, 1, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// TemporalField
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			lblTemporal.setPreferredSize(new Dimension(90, 25));
			p.add(lblTemporal);
			cbTemporalField.setPreferredSize(new Dimension(185, 25));
			p.add(cbTemporalField);
			p.add(btnGetField);
			container.add(p, new GBC(0, 2, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// Pivot Checkbox
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(chkIsPivot);
			container.add(p, new GBC(1, 0, 1, 1).setFill(GBC.BOTH).setWeight(0, 0).setInsets(1));

			// Pivot Field ComboBox
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel lblpf = new JLabel("Pivot Field");
			lblpf.setPreferredSize(new Dimension(70, 25));
			p.add(lblpf);
			cbPivotField.setPreferredSize(new Dimension(120, 25));
			cbPivotField.setEnabled(false);
			p.add(cbPivotField);
			container.add(p, new GBC(1, 1, 1, 1).setFill(GBC.BOTH).setWeight(0, 0).setInsets(1));

			// Pivot Value Field
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel lblpvf = new JLabel("Value Field");
			lblpvf.setPreferredSize(new Dimension(70, 25));
			p.add(lblpvf);
			cbPivotValueField.setPreferredSize(new Dimension(120, 25));
			cbPivotValueField.setEnabled(false);
			p.add(cbPivotValueField);
			container.add(p, new GBC(1, 2, 1, 1).setFill(GBC.BOTH).setWeight(0, 0).setInsets(1));

			// Pivot Value List & Series Field List
			pnlFields = new JPanel();
			pnlFields.setLayout(new FlowLayout(FlowLayout.LEFT));
			lstSeriesFields.setPreferredSize(new Dimension(130, 105));
			jspSeriesFields = new JScrollPane(lstSeriesFields);
			jspSeriesFields.setPreferredSize(new Dimension(150, 105));
			pnlFields.add(jspSeriesFields);
			lstPivotFields.setPreferredSize(new Dimension(130, 105));
			jspPivotFields = new JScrollPane(lstPivotFields);
			jspPivotFields.setPreferredSize(new Dimension(150, 105));
			container.add(pnlFields, new GBC(2, 0, 1, 3).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// Stack Checkbox
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(chkStack);
			container.add(p, new GBC(3, 0, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// FixRange
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel lblFixedAutoRange = new JLabel("Fixed Range");
			lblFixedAutoRange.setPreferredSize(new Dimension(70, 25));
			p.add(lblFixedAutoRange);
			txtFixRange.setPreferredSize(new Dimension(30, 25));
			p.add(txtFixRange);
			p.add(new JLabel("Min."));
			container.add(p, new GBC(3, 1, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));

			// button Plotting& Refresh Pivot Value
			p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(btnRefreshPivotValue);
			p.add(btnPlot);
			container.add(p, new GBC(3, 2, 1, 1).setFill(GBC.BOTH).setWeight(1, 0).setInsets(1));
		}

		private List<String> getShareRealTimeTable(String host, int port) {
			ArrayList<String> result = new ArrayList<String>();
			DBConnection conn = new DBConnection();

			try {
				conn.connect(host, port);
				String sql = "exec name from objs(true) where shared = true,type=`REALTIME";
				BasicStringVector values = (BasicStringVector) conn.run(sql);

				for (int i = 0; i < values.rows(); i++) {
					result.add(values.getString(i));
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			} finally {
				conn.close();
			}
			return result;
		}

		
		private void getTableFields(String tableName){
			if (tableName.isEmpty()) {
				return;
			}
			try {
				DBConnection conn = new DBConnection();

				conn.connect(this.getHost(), this.getPort());
				BasicTable schema = (BasicTable) conn.run("schema(" + tableName + ").colDefs");
				conn.close();
				cbTemporalField.removeAllItems();
				cbPivotField.removeAllItems();
				cbPivotValueField.removeAllItems();
				lstSeriesFields.removeAll();
				for (int i = 0; i < schema.rows(); i++) {
					String colName = schema.getColumn(0).get(i).getString();
					String dataType = schema.getColumn(1).get(i).getString();
					ListItem item = new ListItem(i, colName, dataType);

					if (dataType.equalsIgnoreCase("DATETIME") | dataType.equalsIgnoreCase("TIME")
							| dataType.equalsIgnoreCase("TIMESTAMP") | dataType.equalsIgnoreCase("DATE")
							| dataType.equalsIgnoreCase("MINUTE") | dataType.equalsIgnoreCase("SECOND")) {
						cbTemporalField.addItem(item);
						if (dataType.equalsIgnoreCase("DATE")) {
							cbPivotField.addItem(item);
						}
					} else if (dataType.equalsIgnoreCase("INT") | dataType.equalsIgnoreCase("SHORT")
							| dataType.equalsIgnoreCase("LONG") | dataType.equalsIgnoreCase("FLOAT")
							| dataType.equalsIgnoreCase("DOUBLE")) {
						lstSeriesFields.addItem(item);
						cbPivotValueField.addItem(item);
						if (dataType.equalsIgnoreCase("INT") | dataType.equalsIgnoreCase("SHORT")
								| dataType.equalsIgnoreCase("LONG")) {
							cbPivotField.addItem(item);
						}
					} else if (dataType.equalsIgnoreCase("SYMBOL") | dataType.equalsIgnoreCase("STRING")) {
						cbPivotField.addItem(item);
					}
				}
			} catch (java.net.ConnectException e0) {
				JOptionPane.showMessageDialog(this, e0.getMessage());
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				JOptionPane.showMessageDialog(this, e2.getMessage());
			}
		}
		private List<String> getPivotFieldValue(String host, int port, String tableName, String pivotField) {
			ArrayList<String> result = new ArrayList<String>();
			DBConnection conn = new DBConnection();

			try {
				conn.connect(host, port);
				String sql = "string(exec distinct " + pivotField + " from " + tableName + ")";
				BasicStringVector values = (BasicStringVector) conn.run(sql);

				for (int i = 0; i < values.rows(); i++) {
					result.add(values.getString(i));
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			} finally {
				conn.close();
			}
			return result;
		}

		private boolean checkData() {
			if (this.getHost().isEmpty()) {
				return false;
			}
			if (this.getPort() <= 0) {
				return false;
			}
			return true;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (checkData() == false)
				return;
			if (e.getSource() == btnGetField) {
				getTableFields(this.getTableName());
			} else if (e.getSource() == chkIsPivot) {

				cbPivotField.setEnabled(chkIsPivot.isSelected());
				cbPivotValueField.setEnabled(chkIsPivot.isSelected());
				txtPivotValue.setEnabled(chkIsPivot.isSelected());
				btnRefreshPivotValue.setEnabled(chkIsPivot.isSelected());
				pnlFields.removeAll();
				if (chkIsPivot.isSelected())
					pnlFields.add(jspPivotFields);
				else
					pnlFields.add(jspSeriesFields);
				pnlFields.validate();
				pnlFields.repaint();

			} else if (e.getSource() == btnRefreshPivotValue) {
				if (chkIsPivot.isSelected()) {
					if (this.getTableName().isEmpty()) {
						return;
					}
					List<String> values = getPivotFieldValue(this.getHost(), this.getPort(), this.getTableName(),
							this.getPivotFieldName());
					lstPivotFields.removeAllItems();
					for (String s : values) {
						ListItem item = new ListItem(0, s, "STRING");
						lstPivotFields.addItem(item);
					}
				} else {

				}
			} else if (e.getSource() == btnGetTable) {
				List<String> tables = getShareRealTimeTable(this.getHost(), this.getPort());
				this.cbTableName.removeAllItems();
				for (String t : tables)
					this.cbTableName.addItem(t);
				getTableFields(this.getTableName());
			}

		}

	}
}
