import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import "./AnalyticsChart.css";

const AnalyticsChart = (props) => {
  const { data } = props;

  const chartData = [
    { name: "Дефект 1", value: 8 },
    { name: "Дефект 2", value: 10 },
    { name: "Дефект 3", value: 3 },
  ];

  return (
    <div className="chart-container">
      <h2 className="chart-title">#анализ повреждений наглядно</h2>
      <div className="chart-wrapper">
        <ResponsiveContainer width="100%" height={400}>
          <BarChart
            data={chartData || data}
            margin={{ top: 20, right: 30, left: 40, bottom: 60 }}
            className="custom-chart"
          >
            <CartesianGrid
              strokeDasharray="3 3"
              vertical={false}
              stroke="#965eeb"
            />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              angle={-45}
              textAnchor="end"
              height={70}
            />
            <YAxis tick={{ fontSize: 12 }} axisLine={false} tickLine={false} />
            <Tooltip
              contentStyle={{
                borderRadius: "8px",
                boxShadow: "0 3px 14px rgba(0,0,0,0.15)",
                border: "none",
              }}
            />
            <Legend
              wrapperStyle={{
                paddingTop: "20px",
              }}
            />
            <Bar
              dataKey="value"
              fill="#8884d8"
              name="Вероятность дефекта, %"
              radius={[4, 4, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default AnalyticsChart;
