package com.example.task82.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.task82.R;
import com.example.task82.models.InterviewSession;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView responseTimeText, confidenceLevelText, overallScoreText;
    private TextView sessionsCompletedText, averageScoreText, improvementTrendText;
    private LineChart responseTimeChart;
    private BarChart confidenceChart;
    private PieChart performanceChart;
    
    // Dynamic data storage
    private List<InterviewSession> recentSessions = new ArrayList<>();
    private List<Double> sessionScores = new ArrayList<>();
    private List<String> sessionCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        initializeViews(view);
        loadDynamicData();
        setupCharts();
        updateDashboardMetrics();
        
        return view;
    }

    private void initializeViews(View view) {
        responseTimeText = view.findViewById(R.id.response_time_text);
        confidenceLevelText = view.findViewById(R.id.confidence_level_text);
        overallScoreText = view.findViewById(R.id.overall_score_text);
        
        sessionsCompletedText = view.findViewById(R.id.sessions_completed_text);
        averageScoreText = view.findViewById(R.id.average_score_text);
        improvementTrendText = view.findViewById(R.id.improvement_trend_text);
        
        responseTimeChart = view.findViewById(R.id.response_time_chart);
        confidenceChart = view.findViewById(R.id.confidence_chart);
        performanceChart = view.findViewById(R.id.performance_chart);
    }
    
    private void loadDynamicData() {
        // In a real app, this would load from SharedPreferences, Database, or API
        // For now, we'll simulate some dynamic data
        
        // Simulate recent session data
        simulateSessionData();
    }
    
    private void simulateSessionData() {
        // Add some sample session data that would come from actual interviews
        sessionScores.add(78.5);
        sessionScores.add(82.3);
        sessionScores.add(85.1);
        sessionScores.add(79.8);
        sessionScores.add(88.2);
        
        sessionCategories.add("Software Engineer");
        sessionCategories.add("Data Analyst");
        sessionCategories.add("Behavioral");
        sessionCategories.add("Product Manager");
        sessionCategories.add("Behavioral");
    }
    
    private void updateDashboardMetrics() {
        // Calculate dynamic metrics
        double averageScore = sessionScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        int totalSessions = sessionScores.size();
        
        // Calculate improvement trend
        String improvementTrend = calculateImprovementTrend();
        
        // Update UI with dynamic data
        if (sessionsCompletedText != null) {
            sessionsCompletedText.setText("Sessions: " + totalSessions);
        }
        
        if (averageScoreText != null) {
            averageScoreText.setText(String.format("Avg Score: %.1f", averageScore));
        }
        
        if (improvementTrendText != null) {
            improvementTrendText.setText("Trend: " + improvementTrend);
        }
        
        // Update existing metrics
        responseTimeText.setText("Average: 1.2 seconds");
        confidenceLevelText.setText("Average: " + String.format("%.0f%%", averageScore));
        overallScoreText.setText("Score: " + String.format("%.0f", averageScore));
    }
    
    private String calculateImprovementTrend() {
        if (sessionScores.size() < 2) {
            return "Insufficient data";
        }
        
        // Compare first half with second half of sessions
        int midPoint = sessionScores.size() / 2;
        double firstHalfAvg = sessionScores.subList(0, midPoint).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double secondHalfAvg = sessionScores.subList(midPoint, sessionScores.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double improvement = secondHalfAvg - firstHalfAvg;
        
        if (improvement > 2) {
            return "📈 Improving";
        } else if (improvement < -2) {
            return "📉 Declining";
        } else {
            return "📊 Stable";
        }
    }

    private void setupCharts() {
        setupResponseTimeChart();
        setupConfidenceChart();
        setupPerformanceChart();
    }

    private void setupResponseTimeChart() {
        List<Entry> entries = new ArrayList<>();
        
        // Use dynamic data if available, otherwise use sample data
        if (!sessionScores.isEmpty()) {
            for (int i = 0; i < Math.min(sessionScores.size(), 5); i++) {
                // Simulate response time based on score (higher score = faster response)
                float responseTime = (float) (2.0 - (sessionScores.get(i) / 100.0));
                entries.add(new Entry(i, Math.max(0.5f, responseTime)));
            }
        } else {
            // Fallback sample data
            entries.add(new Entry(0f, 1.5f));
            entries.add(new Entry(1f, 1.3f));
            entries.add(new Entry(2f, 1.1f));
            entries.add(new Entry(3f, 1.2f));
            entries.add(new Entry(4f, 1.0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Response Time (seconds)");
        dataSet.setColor(getResources().getColor(R.color.chart_blue, null));
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        responseTimeChart.setData(lineData);
        responseTimeChart.getDescription().setEnabled(false);
        responseTimeChart.invalidate();
    }

    private void setupConfidenceChart() {
        List<BarEntry> entries = new ArrayList<>();
        
        // Use dynamic session scores
        if (!sessionScores.isEmpty()) {
            for (int i = 0; i < Math.min(sessionScores.size(), 5); i++) {
                entries.add(new BarEntry(i, sessionScores.get(i).floatValue()));
            }
        } else {
            // Fallback sample data
            entries.add(new BarEntry(0f, 70f));
            entries.add(new BarEntry(1f, 75f));
            entries.add(new BarEntry(2f, 80f));
            entries.add(new BarEntry(3f, 85f));
            entries.add(new BarEntry(4f, 85f));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Performance Score (%)");
        dataSet.setColor(getResources().getColor(R.color.chart_green, null));
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));

        BarData barData = new BarData(dataSet);
        confidenceChart.setData(barData);
        confidenceChart.getDescription().setEnabled(false);
        confidenceChart.invalidate();
    }

    private void setupPerformanceChart() {
        List<PieEntry> entries = new ArrayList<>();
        
        // Calculate performance distribution
        double averageScore = sessionScores.stream().mapToDouble(Double::doubleValue).average().orElse(78.0);
        
        entries.add(new PieEntry((float) averageScore, "Current Score"));
        entries.add(new PieEntry((float) (100 - averageScore), "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "Performance");
        dataSet.setColors(
            getResources().getColor(R.color.chart_blue, null),
            getResources().getColor(R.color.border_light, null)
        );
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary, null));
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        performanceChart.setData(pieData);
        performanceChart.getDescription().setEnabled(false);
        performanceChart.setDrawHoleEnabled(true);
        performanceChart.setHoleRadius(40f);
        performanceChart.invalidate();
    }
    
    // Method to update dashboard with new session data
    public void addSessionData(double score, String category) {
        sessionScores.add(score);
        sessionCategories.add(category);
        
        // Refresh the dashboard
        if (getView() != null) {
            updateDashboardMetrics();
            setupCharts();
        }
    }
    
    // Method to refresh dashboard when fragment becomes visible
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when user returns to dashboard
        loadDynamicData();
        updateDashboardMetrics();
        setupCharts();
    }
} 