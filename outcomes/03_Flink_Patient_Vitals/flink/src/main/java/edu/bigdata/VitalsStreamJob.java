package edu.bigdata;

import java.io.Serializable;
import java.time.Duration;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/** Finite synthetic device stream with event-time alerts and a 10-second summary. */
public class VitalsStreamJob {
  public static class Reading implements Serializable {
    public String patientId;
    public long eventTime;
    public int heartRate;
    public int spo2;
    public double temperature;

    public Reading() {}

    public Reading(String patientId, long eventTime, int heartRate, int spo2, double temperature) {
      this.patientId = patientId;
      this.eventTime = eventTime;
      this.heartRate = heartRate;
      this.spo2 = spo2;
      this.temperature = temperature;
    }

    public boolean abnormal() {
      return heartRate > 100 || spo2 < 92 || temperature > 38.0;
    }

    public String reasons() {
      StringBuilder out = new StringBuilder();
      if (heartRate > 100) out.append("heart_rate_demo_high ");
      if (spo2 < 92) out.append("spo2_demo_low ");
      if (temperature > 38.0) out.append("temperature_demo_high ");
      return out.toString().trim();
    }
  }

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    DataStream<Reading> readings = env.addSource(new SyntheticVitalsSource())
        .assignTimestampsAndWatermarks(WatermarkStrategy
            .<Reading>forBoundedOutOfOrderness(Duration.ofSeconds(1))
            .withTimestampAssigner((reading, ignored) -> reading.eventTime));

    readings.filter(Reading::abnormal)
        .map(r -> "patient=" + r.patientId + " flags=" + r.reasons()
            + " hr=" + r.heartRate + " spo2=" + r.spo2 + " tempC=" + r.temperature)
        .print("ALERT");

    readings.keyBy(r -> r.patientId)
        .window(TumblingEventTimeWindows.of(Time.seconds(10)))
        .process(new ProcessWindowFunction<Reading, String, String, TimeWindow>() {
          @Override
          public void process(String patient, Context context, Iterable<Reading> values,
                              Collector<String> out) {
            int total = 0;
            int abnormal = 0;
            for (Reading r : values) {
              total++;
              if (r.abnormal()) abnormal++;
            }
            out.collect("patient=" + patient + " window=[" + context.window().getStart()
                + "," + context.window().getEnd() + ") readings=" + total
                + " abnormal=" + abnormal);
          }
        })
        .print("SUMMARY");

    env.execute("Synthetic patient vitals demonstration");
  }

  private static class SyntheticVitalsSource implements SourceFunction<Reading> {
    private volatile boolean running = true;

    @Override
    public void run(SourceContext<Reading> ctx) throws Exception {
      // Place the finite 10-event sample inside one 10-second event-time window.
      long base = (System.currentTimeMillis() / 10_000L) * 10_000L;
      int[] hr = {78, 112, 82, 75, 118, 80, 77, 79, 83, 76};
      int[] spo2 = {98, 96, 89, 97, 94, 98, 97, 99, 96, 98};
      double[] temp = {36.8, 37.1, 36.9, 38.2, 37.3, 36.7, 36.8, 36.9, 36.7, 36.8};
      String[] patient = {"P101", "P102", "P101", "P103", "P102", "P103", "P101", "P102", "P103", "P101"};
      for (int i = 0; i < hr.length && running; i++) {
        synchronized (ctx.getCheckpointLock()) {
          ctx.collect(new Reading(patient[i], base + i * 500L, hr[i], spo2[i], temp[i]));
        }
        Thread.sleep(500L);
      }
      // The bounded synthetic source advances event time so the final window is emitted.
      ctx.emitWatermark(new Watermark(Long.MAX_VALUE));
    }

    @Override
    public void cancel() {
      running = false;
    }
  }
}
