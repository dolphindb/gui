	package com.xxdb.gui.component;

	import java.awt.*;
	import java.text.*;
	import java.util.*;
	import java.util.List;

	import com.xxdb.data.*;
	import org.jfree.chart.ChartFactory;
	import org.jfree.chart.JFreeChart;
	import org.jfree.chart.axis.*;
	import org.jfree.chart.labels.*;
	import org.jfree.chart.plot.*;
	import org.jfree.chart.renderer.xy.*;
	import org.jfree.data.category.DefaultCategoryDataset;
	import org.jfree.data.time.*;
	import org.jfree.data.xy.*;

	import com.xxdb.data.Chart.CHART_TYPE;
	import com.xxdb.gui.data.XXDBChartData;

	public class XXDBChart {
		public enum X_AXIS {CATEGORY, DATE, XY}

		;

		private static final String[] day_formats = {
				"yyyyMMdd", "yyyy.MM.dd", "yyyy/MM/dd", "yyyy-MM-dd",
		};
		private static final String[] minute_formats = {"HH:mm"};
		private static final String[] second_formats = {
				"HH:mm:ss",
				"MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss", "MM/dd/yyyy HH:mm:ss",
				"yyyy.MM.dd'T'HH:mm:ss'Z'", "yyyy.MM.dd'T'HH:mm:ssZ", "yyyy.MM.dd'T'HH:mm:ss",
				"yyyy:MM:dd HH:mm:ss",
		};
		private static final String[] month_formats = {
				"yyyy.MM'M'"
		};
		private static final String[] millisecond_formats = {
				"HH:mm:ss.SSS",
				"HH:mm:ss.SSSSSSSSS", "HH:mm:ss.SSSSSSSSS'Z'", "HH:mm:ss.SSSSSSSSSZ",
				"yyyy.MM.dd'T'HH:mm:ss.SSS'Z'",
				"yyyy.MM.dd'T'HH:mm:ss.SSSZ",
				"yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSS'Z'", "yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSS",
				"yyyy.MM.dd'T'HH:mm:ss.SSSSSSSSSZ",
				"MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS"
		};

		public static RegularTimePeriod getUtilDateForTimeSeries(String dt) {
			for (String parse : millisecond_formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				sdf.setLenient(false);
				try {
					Date d = sdf.parse(dt);
					return new Millisecond(d);
				} catch (ParseException e) {
					// Do nothing
				}
			}
			for (String parse : second_formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				sdf.setLenient(false);
				try {
					Date d = sdf.parse(dt);
					return new Second(d);
				} catch (ParseException e) {
					// Do nothing
				}
			}
			for (String parse : month_formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				sdf.setLenient(false);
				try {
					Date d = sdf.parse(dt);
					return new Month(d);
				} catch (ParseException e) {
					// Do nothing
				}
			}
			for (String parse : minute_formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				sdf.setLenient(false);
				try {
					Date d = sdf.parse(dt);
					return new Minute(d);
				} catch (ParseException e) {
					// Do nothing
				}
			}
			for (String parse : day_formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				sdf.setLenient(false);
				try {
					Date d = sdf.parse(dt);
					return new Day(d);
				} catch (ParseException e) {
					// Do nothing
				}
			}
			return null;
		}


		//FIXME: label not shown completely
		public static JFreeChart getChart(BasicChart chart, XXDBChartData tbl)
				throws Exception {
			Boolean isStacking = ((BasicBoolean) chart.get(new BasicString("stacking"))).getBoolean();
			if (CHART_TYPE.CT_LINE == chart.getChartType()) {
				if (isStacking) {
					return getStackedLineChart(chart, tbl);
				}
				if (tbl.getxAxisType() == X_AXIS.DATE) {
					return getTimeSeriesChart(chart, tbl);
				} else {
					return getLineChart(chart, tbl);
				}
			} else if (CHART_TYPE.CT_BAR == chart.getChartType()) {
				if (isStacking) {
					return getStackedBarChart(chart, tbl, PlotOrientation.VERTICAL);
				}
				return getBarChart(chart, tbl, PlotOrientation.VERTICAL);
			} else if (CHART_TYPE.CT_COLUMN == chart.getChartType()) {
				return getBarChart(chart, tbl, PlotOrientation.HORIZONTAL);
			} else if (CHART_TYPE.CT_SCATTER == chart.getChartType()) {
				return getScatterChart(chart, tbl);
			} else if (CHART_TYPE.CT_AREA == chart.getChartType()) {
				if (isStacking) {
					return getStackedAreaChart(chart, tbl);
				}
				return getAreaChart(chart, tbl);
			} else if(CHART_TYPE.CT_PIE == chart.getChartType()) {
				JFreeChart piechart = ChartFactory.createPieChart(
						chart.getTitle(),
						tbl.getPieset(),
						true, true, false);

				PiePlot plot = (PiePlot) piechart.getPlot();
				plot.setNoDataMessage("No data available");
				plot.setCircular(false);
				plot.setLabelGap(0.02);

				return piechart;
			} else if(CHART_TYPE.CT_HISTOGRAM == chart.getChartType()) {
				JFreeChart histChart = ChartFactory.createHistogram(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getHistogramSet(),
						PlotOrientation.VERTICAL,
						false, false, false);

				return histChart;
			} else if(CHART_TYPE.CT_KLINE == chart.getChartType()) {
				return getKLineChart(chart, tbl);
			}
			return null;
		}

		private static JFreeChart getLineChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart lineChart = null;

			if (tbl.getxAxisType() == X_AXIS.CATEGORY) {
				lineChart = ChartFactory.createLineChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getDataSet(),
						PlotOrientation.VERTICAL,
						chart.getData().hasColumnLabel(), true, false);
			} else if (tbl.getxAxisType() == X_AXIS.XY) {
				//todo：get param XY_MULTI_AXIS from server

				BasicBoolean XY_MULTI_AXIS = (BasicBoolean) (chart.getExtraParameter(CHART_PARAMETER_TYPE.multiYAxes));
				if (XY_MULTI_AXIS.getBoolean()) {
					List<XYSeries> series = tbl.getXYSeries();

					List<XYSeriesCollection> datasets = new ArrayList<>();
					for (int i = 0; i < series.size(); i++) {
						datasets.add(new XYSeriesCollection());
					}

					for (int i = 0; i < series.size(); i++) {
						XYSeries s = series.get(i);
						XYSeriesCollection dataset = datasets.get(i);
						dataset.addSeries(s);
					}

					lineChart = ChartFactory.createXYLineChart(
							chart.getTitle(),
							chart.getXAxisName(),
							chart.getYAxisName(),
							datasets.get(0),
							PlotOrientation.VERTICAL,
							chart.getData().hasColumnLabel(), true, false);

					XYPlot plot = lineChart.getXYPlot();

					for (int i = 1; i < series.size(); i++) {
						NumberAxis axis = new NumberAxis();
						plot.setRangeAxis(i, axis);
						plot.setDataset(i, datasets.get(i));
						plot.mapDatasetToRangeAxis(i, i);
						XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
						// setting tips
						render.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
						plot.setRenderer(i, render);
					}
				} else {
					XYSeriesCollection dataset = new XYSeriesCollection();

					List<XYSeries> series = tbl.getXYSeries();
					for (XYSeries s : series) {
						dataset.addSeries(s);
					}

					lineChart = ChartFactory.createXYLineChart(
							chart.getTitle(),
							chart.getXAxisName(),
							chart.getYAxisName(),
							dataset,
							PlotOrientation.VERTICAL,
							chart.getData().hasColumnLabel(), true, false);
				}
			}
			return lineChart;
		}

		private static JFreeChart getStackedLineChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart stackedLineChart = null;
			if (tbl.getxAxisType() == X_AXIS.DATE) {
				TimeTableXYDataset dataset = new TimeTableXYDataset();
				List<TimeSeries> series = tbl.getSeries();
				for (TimeSeries s : series) {
					String key = s.getKey().toString();
					for (Object item:s.getItems()) {
						dataset.add(((TimeSeriesDataItem) item).getPeriod(), ((TimeSeriesDataItem) item).getValue(), key, false);
					}
				}
				DateAxis domain = new DateAxis(chart.getXAxisName());
				domain.setLowerMargin(0.2D);
				domain.setUpperMargin(0.2D);
				NumberAxis range = new NumberAxis(chart.getYAxisName());
				XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						DateFormat.getDateInstance(), NumberFormat.getInstance());
				StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(toolTipGenerator, null);
				XYPlot plot = new XYPlot(dataset, domain, range, renderer);
				plot.setForegroundAlpha(0.5F);
				plot.setOrientation(PlotOrientation.VERTICAL);
				stackedLineChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			} else if (tbl.getxAxisType() == X_AXIS.CATEGORY || tbl.getxAxisType() == X_AXIS.XY) {
				return getStackedAreaChart(chart, tbl);
			}
			return stackedLineChart;
		}

		private static JFreeChart getBarChart(BasicChart chart, XXDBChartData tbl, PlotOrientation po) {
			JFreeChart barChart = null;

			if (tbl.getxAxisType() == X_AXIS.CATEGORY || tbl.getxAxisType() == X_AXIS.DATE) {
				barChart = ChartFactory.createBarChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getDataSet(),
						po,
						chart.getData().hasColumnLabel(), true, false);
			} else if (tbl.getxAxisType() == X_AXIS.XY) {
				XYSeriesCollection dataset = new XYSeriesCollection();

				List<XYSeries> series = tbl.getXYSeries();
				double barWidth = 0, barMax = 0, barMin = Double.MAX_VALUE;
				for (XYSeries s : series) {
					dataset.addSeries(s);
					for (int i = 0; i < s.getItemCount(); i++) {
						double val = s.getX(i).doubleValue();
						if (val > barMax) barMax = val;
						if (val < barMin) barMin = val;
					}
					barWidth = (barMax - barMin) / s.getItemCount();
				}

				IntervalXYDataset barset = new XYBarDataset(dataset, barWidth);

				NumberAxis domain = new NumberAxis(chart.getXAxisName());
				domain.setAutoRangeIncludesZero(false);
				NumberAxis range = new NumberAxis(chart.getYAxisName());
				ClusteredXYBarRenderer renderer = new ClusteredXYBarRenderer(0.05D, false);
				renderer.setToolTipGenerator(new StandardXYToolTipGenerator());

				XYPlot plot = new XYPlot(barset, domain, range, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				barChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			}

			return barChart;
		}

		private static JFreeChart getStackedBarChart(BasicChart chart, XXDBChartData tbl, PlotOrientation po) {
			JFreeChart stackedBarChart = null;

			if (tbl.getxAxisType() == X_AXIS.CATEGORY) {
				stackedBarChart = ChartFactory.createStackedBarChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getDataSet(),
						po,
						chart.getData().hasColumnLabel(), true, false);
			} else if (tbl.getxAxisType() == X_AXIS.XY) {
				CategoryTableXYDataset dataset = new CategoryTableXYDataset();
				List<XYSeries> series = tbl.getXYSeries();
				for (XYSeries s : series) {
					Comparable key = s.getKey();
					for (Object item:s.getItems()) {
						dataset.add(((XYDataItem) item).getX(), ((XYDataItem) item).getY(), key.toString(), false);
					}
				}
				NumberAxis domain = new NumberAxis(chart.getXAxisName());
				domain.setAutoRangeIncludesZero(false);
				NumberAxis range = new NumberAxis(chart.getYAxisName());
				StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.1D);
				renderer.setToolTipGenerator(new StandardXYToolTipGenerator("{0} : {1}, {2}", NumberFormat.getIntegerInstance(), NumberFormat.getNumberInstance()));
				XYPlot plot = new XYPlot(dataset, domain, range, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				stackedBarChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			} else if (tbl.getxAxisType() == X_AXIS.DATE) {
				StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.5);
				renderer.setToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						DateFormat.getInstance(), NumberFormat.getInstance()));
				XYPlot plot = getDatePlot(chart, tbl, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				stackedBarChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			}

			return stackedBarChart;

		}

		private static JFreeChart getAreaChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart areaChart = null;
			if (tbl.getxAxisType() == X_AXIS.CATEGORY) {
				areaChart = ChartFactory.createAreaChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getDataSet(),
						PlotOrientation.VERTICAL,
						true, true, false);
				CategoryPlot plot = (CategoryPlot) areaChart.getPlot();
				plot.setForegroundAlpha(0.3F);
			} else if (tbl.getxAxisType() == X_AXIS.XY) {
				XYSeriesCollection dataset = new XYSeriesCollection();

				List<XYSeries> series = tbl.getXYSeries();
				for (XYSeries s : series) {
					dataset.addSeries(s);
				}

				areaChart = ChartFactory.createXYAreaChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						dataset,
						PlotOrientation.VERTICAL,
						chart.getData().hasColumnLabel(), true, false);
			} else if (tbl.getxAxisType() == X_AXIS.DATE) {
				XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("{0} : {1}, {2}",
						DateFormat.getInstance(), NumberFormat.getNumberInstance());
				XYAreaRenderer2 renderer = new XYAreaRenderer2(toolTipGenerator, null);
				XYPlot plot = getDatePlot(chart, tbl, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				plot.setForegroundAlpha(0.3F);
				areaChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			}

			return areaChart;
		}

		private static JFreeChart getStackedAreaChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart stackedAreaChart = null;
			if (tbl.getxAxisType() == X_AXIS.CATEGORY) {
				stackedAreaChart = ChartFactory.createStackedAreaChart(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						tbl.getDataSet(),
						PlotOrientation.VERTICAL,
						true, true, false);
				return stackedAreaChart;
			}
			if (tbl.getxAxisType() == X_AXIS.XY) {
				CategoryTableXYDataset dataset = new CategoryTableXYDataset();
				List<XYSeries> series = tbl.getXYSeries();
				for (XYSeries s : series) {
					Comparable key = s.getKey();
					for (Object item:s.getItems()) {
						dataset.add(((XYDataItem) item).getX(), ((XYDataItem) item).getY(), key.toString(), false);
					}
				}

				//reconstruct ChartFactory
				NumberAxis domain = new NumberAxis(chart.getXAxisName());
				domain.setAutoRangeIncludesZero(false);
				domain.setLowerMargin(0.05D);
				domain.setUpperMargin(0.05D);
				NumberAxis range = new NumberAxis(chart.getYAxisName());
				XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("{0} : {1}, {2}",
						DateFormat.getInstance(), NumberFormat.getNumberInstance());
				StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(toolTipGenerator, null);
				renderer.setFillPaint(Color.white);
				renderer.setOutline(true);
				XYPlot plot = new XYPlot(dataset, domain, range, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				stackedAreaChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			} else if (tbl.getxAxisType() == X_AXIS.DATE) {
				XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("{0} : {1}, {2}",
						DateFormat.getInstance(), NumberFormat.getNumberInstance());
				StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(toolTipGenerator, null);
				XYPlot plot = getDatePlot(chart, tbl, renderer);
				plot.setOrientation(PlotOrientation.VERTICAL);
				stackedAreaChart = new JFreeChart(
						chart.getTitle(),
						JFreeChart.DEFAULT_TITLE_FONT,
						plot,
						chart.getData().hasColumnLabel());
			}

			return stackedAreaChart;
		}

		private static JFreeChart getScatterChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart scatterChart = null;

			if (tbl.getxAxisType() == X_AXIS.XY) {
				XYSeriesCollection dataset = new XYSeriesCollection();

				List<XYSeries> series = tbl.getXYSeries();
				for (XYSeries s : series) {
					dataset.addSeries(s);
				}

				scatterChart = ChartFactory.createScatterPlot(
						chart.getTitle(),
						chart.getXAxisName(),
						chart.getYAxisName(),
						dataset,
						PlotOrientation.VERTICAL,
						chart.getData().hasColumnLabel(), true, false);
			}

			return scatterChart;
		}

		private static JFreeChart getTimeSeriesChart(BasicChart chart, XXDBChartData tbl) {
			XYPlot plot = null;
			JFreeChart tchart = null;

			List<TimeSeries> series = tbl.getSeries();
			int i = 0;
			for (TimeSeries s : series) {
				if (i == 0) {
					tchart = ChartFactory.createTimeSeriesChart(
							chart.getTitle(),
							chart.getXAxisName(), chart.getYAxisName(),
							new TimeSeriesCollection(series.get(0)), chart.getData().hasColumnLabel(), true, false
					);
					tchart.setBackgroundPaint(Color.white);
					plot = tchart.getXYPlot();
					++i;
				} else {
					plot.setDataset(i, new TimeSeriesCollection(s));
					plot.setRenderer(i, new StandardXYItemRenderer());
					++i;
				}
			}

			return tchart;
		}

		private static JFreeChart getKLineChart(BasicChart chart, XXDBChartData tbl) {
			JFreeChart kLineChart = null;

			List<TimeSeries> series = tbl.getSeries();

			int itemsCnt = series.get(0).getItemCount();

			Date[] date = new Date[itemsCnt];
			double[] high = new double[itemsCnt];
			double[] low = new double[itemsCnt];
			double[] open = new double[itemsCnt];
			double[] close = new double[itemsCnt];
			double[] volume = new double[itemsCnt];

			for (int i = 0; i < itemsCnt; i++) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(series.get(0).getTimePeriod(i).getEnd());
				cal.add(Calendar.DATE, -1);
				date[i] = cal.getTime();
				high[i] = series.get(1).getValue(i).doubleValue();
				low[i] = series.get(2).getValue(i).doubleValue();
				open[i] = series.get(0).getValue(i).doubleValue();
				close[i] = series.get(3).getValue(i).doubleValue();
				volume[i] = series.get(4).getValue(i).doubleValue();
			}

			DefaultHighLowDataset dataset = new DefaultHighLowDataset("Candle Stick", date, high, low, open, close, volume);

			kLineChart = ChartFactory.createCandlestickChart(
					chart.getTitle(),
					chart.getXAxisName(),
					chart.getYAxisName(),
					dataset,
					true
			);


			return kLineChart;
		}

		private static XYPlot getDatePlot(BasicChart chart, XXDBChartData tbl, XYItemRenderer renderer) {
			TimeTableXYDataset dataset = new TimeTableXYDataset();
			DefaultCategoryDataset inputDataset = tbl.getDataSet();
			int cols = inputDataset.getColumnCount(), rows = inputDataset.getRowCount();
			for (int i = 0; i < rows; i++) {
				String key = inputDataset.getRowKey(i).toString();
				for (int j = 0; j < cols; j++) {
					String date = inputDataset.getColumnKey(j).toString();
					RegularTimePeriod timePeriod = getUtilDateForTimeSeries(date);
					Number value = inputDataset.getValue(i, j);
					dataset.add(timePeriod, value, key, false);
				}
			}
			DateAxis domain = new DateAxis(chart.getXAxisName());
			domain.setLowerMargin(0.2D);
			domain.setUpperMargin(0.2D);
			NumberAxis range = new NumberAxis(chart.getYAxisName());
			XYPlot plot = new XYPlot(dataset, domain, range, renderer);
			return plot;
		}
	}

