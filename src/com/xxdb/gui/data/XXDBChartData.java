package com.xxdb.gui.data;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;

import com.xxdb.data.BasicChart;
import com.xxdb.data.BasicIntVector;
import com.xxdb.data.BasicString;
import com.xxdb.data.Chart.CHART_TYPE;
import com.xxdb.data.Entity;
import com.xxdb.data.Entity.DATA_CATEGORY;
import com.xxdb.data.Matrix;
import com.xxdb.data.Scalar;
import com.xxdb.data.Vector;
import com.xxdb.gui.component.XXDBChart;
import com.xxdb.gui.component.XXDBChart.X_AXIS;

public class XXDBChartData {	
	private List<TimeSeries> series = new ArrayList<TimeSeries>();
	private DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
	private DefaultPieDataset pieset = new DefaultPieDataset();
	private List<XYSeries> xySeries = new ArrayList<XYSeries>();
	private HistogramDataset histogramSet = new HistogramDataset();
	private X_AXIS xAxisType;
	
	public List<TimeSeries> getSeries() {
		return series;
	}
	
	public List<XYSeries> getXYSeries() {
		return xySeries;
	}
	
	public X_AXIS getxAxisType() {
		return xAxisType;
	}

	public DefaultCategoryDataset getDataSet() {
		return dataset;
	}

	public DefaultPieDataset getPieset() {
		return pieset;
	}
	
	public HistogramDataset getHistogramSet() {
		return histogramSet;
	}
	
	public void loadTable(BasicChart c) throws Exception {
		Matrix m = c.getData();
		int columns = m.columns();
		int rows = m.rows();
		RegularTimePeriod temporalPeriod = null;
		
		if(c.getChartType() == CHART_TYPE.CT_SCATTER){
			xAxisType = X_AXIS.XY;
		} 
		else if(m.hasRowLabel()) {
			// Check if the row label can be used for time series graph
			if((temporalPeriod = XXDBChart.getUtilDateForTimeSeries(m.getRowLabel(0).getString())) != null){
				xAxisType = X_AXIS.DATE;
			} 
			else {
				DATA_CATEGORY cat = m.getRowLabel(0).getDataCategory();
				if(cat == DATA_CATEGORY.FLOATING || cat == DATA_CATEGORY.INTEGRAL)
					xAxisType = X_AXIS.XY;
				else
					xAxisType = X_AXIS.CATEGORY;
			}
		} 
		else {
			xAxisType = c.getChartType() == CHART_TYPE.CT_LINE ? X_AXIS.XY : X_AXIS.CATEGORY;
		}
				
		boolean hasColumnLabel = m.hasColumnLabel();
		boolean hasRowLabel = m.hasRowLabel();
		Vector rowLabels = null;
		double[] histValues = null;
		
		if(hasRowLabel)
			rowLabels = m.getRowLabels();
		else if(xAxisType == X_AXIS.XY){
			BasicIntVector labels = new BasicIntVector(rows);
			for(int i=0; i<rows; ++i)
				labels.setInt(i, i);
			rowLabels = labels;
			hasRowLabel = true;
		}
		
		for(int i = 0; i < columns; ++i) {
			String cLabel = hasColumnLabel ? m.getColumnLabel(i).getString() : "";
			
			TimeSeries s = null;
			XYSeries xys = null;
			if(xAxisType == X_AXIS.DATE)
				s = new TimeSeries(cLabel, temporalPeriod.getClass());
			else if(xAxisType == X_AXIS.XY)
				xys = new XYSeries(cLabel);
			
			if(c.getChartType() == CHART_TYPE.CT_HISTOGRAM)
				histValues = new double[rows];
			
			for(int j = 0; j < rows; ++j) {
				Scalar rLabel = hasRowLabel ? (Scalar)rowLabels.get(j) : new BasicString("D" +j);
				Scalar val = m.get(j, i);
				
				if(!val.isNull()) {
					if((c.getChartType() == CHART_TYPE.CT_LINE || c.getChartType() == CHART_TYPE.CT_KLINE) && xAxisType == X_AXIS.DATE){
							// rLabel is assumed to be a date/time at this point
							RegularTimePeriod p = XXDBChart.getUtilDateForTimeSeries(rLabel.getString());
							s.add(p, val.getNumber().doubleValue());
					} else if(xAxisType == X_AXIS.XY) {
						xys.add(rLabel.getNumber().doubleValue(), val.getNumber().doubleValue());
					} else if(c.getChartType() == CHART_TYPE.CT_PIE) {
						pieset.setValue(rLabel.getString(), val.getNumber().doubleValue());
					} else if(c.getChartType() == CHART_TYPE.CT_HISTOGRAM) {
						histValues[j] = val.getNumber().doubleValue();
					} else {
						dataset.addValue(val.getNumber().doubleValue(), cLabel, rLabel.getString());
					}
				}				
			}
			if(xAxisType == X_AXIS.DATE)
				series.add(s);
			else if(xAxisType == X_AXIS.XY)
				xySeries.add(xys);
			else if(c.getChartType() == CHART_TYPE.CT_HISTOGRAM){
				int binCount = 50;
				Entity binCountObj = c.get(new BasicString("binCount"));
				if(binCountObj != null)
					binCount = ((Scalar)binCountObj).getNumber().intValue();
				Entity binStartObj = c.get(new BasicString("binStart"));
				Entity binEndObj = c.get(new BasicString("binEnd"));
				if(binStartObj == null)
					histogramSet.addSeries(cLabel, histValues, binCount);
				else{
					double binStart = ((Scalar)binStartObj).getNumber().doubleValue();
					double binEnd = ((Scalar)binEndObj).getNumber().doubleValue();
					histogramSet.addSeries(cLabel, histValues, binCount, binStart, binEnd);
				}
			}
		}
	}	
}
