import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Draws a dependency-free PNG overview of hourly requests and status codes. */
public class CloudLogChart {
  public static void main(String[] args) throws Exception {
    Path input = Paths.get(args.length > 0 ? args[0] : "data/server_logs.csv");
    Path output = Paths.get(args.length > 1 ? args[1] : "outputs/cloud-log-chart.png");
    Map<String,Integer> hours = new TreeMap<>();
    Map<String,Integer> statuses = new TreeMap<>();
    try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
      String line = reader.readLine();
      while ((line = reader.readLine()) != null) {
        String[] cells = line.split(",", -1);
        if (cells.length < 6) continue;
        String hour = cells[0].substring(0, 13).substring(11);
        hours.merge(hour, 1, Integer::sum);
        statuses.merge(cells[1], 1, Integer::sum);
      }
    }
    int width = 1100, height = 610;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Color.WHITE); g.fillRect(0, 0, width, height);
    g.setColor(new Color(18,48,74)); g.setFont(new Font("SansSerif", Font.BOLD, 26));
    g.drawString("Synthetic server log overview", 48, 46);
    drawBars(g, hours, "Requests by hour (UTC)", 55, 95, 470, 430, new Color(20,126,133));
    drawBars(g, statuses, "HTTP status counts", 575, 95, 470, 430, new Color(57,112,170));
    g.setColor(new Color(86,99,111)); g.setFont(new Font("SansSerif", Font.PLAIN, 14));
    g.drawString("Synthetic sample only  |  Total requests: " + hours.values().stream().mapToInt(Integer::intValue).sum(), 48, 570);
    g.dispose();
    Files.createDirectories(output.getParent());
    ImageIO.write(image, "png", output.toFile());
    System.out.println("chart=" + output.toAbsolutePath());
  }

  private static void drawBars(Graphics2D g, Map<String,Integer> values, String title,
                               int x, int y, int w, int h, Color color) {
    g.setColor(new Color(18,48,74)); g.setFont(new Font("SansSerif", Font.BOLD, 18));
    g.drawString(title, x, y);
    int top = y + 32, bottom = y + h - 48, left = x + 45, right = x + w - 15;
    int max = Math.max(1, values.values().stream().mapToInt(Integer::intValue).max().orElse(1));
    g.setFont(new Font("SansSerif", Font.PLAIN, 13));
    for (int tick = 0; tick <= max; tick++) {
      int gy = bottom - (bottom - top) * tick / max;
      g.setColor(new Color(226,232,238)); g.drawLine(left, gy, right, gy);
      g.setColor(new Color(82,95,108)); g.drawString(Integer.toString(tick), x + 8, gy + 5);
    }
    g.setColor(new Color(115,128,140)); g.drawLine(left, top, left, bottom); g.drawLine(left, bottom, right, bottom);
    int n = Math.max(1, values.size()), slot = (right - left) / n, bar = Math.min(52, slot / 2);
    int i = 0;
    for (Map.Entry<String,Integer> e : values.entrySet()) {
      int bh = (bottom - top) * e.getValue() / max;
      int bx = left + i * slot + (slot - bar) / 2;
      int by = bottom - bh;
      g.setColor(color); g.fillRoundRect(bx, by, bar, bh, 8, 8);
      g.setColor(new Color(18,48,74)); g.setFont(new Font("SansSerif", Font.BOLD, 13));
      g.drawString(Integer.toString(e.getValue()), bx + bar / 2 - 4, by - 7);
      g.setFont(new Font("SansSerif", Font.PLAIN, 12));
      String label = e.getKey(); int lw = g.getFontMetrics().stringWidth(label);
      g.drawString(label, bx + bar / 2 - lw / 2, bottom + 22);
      i++;
    }
  }
}
