package gr.ihu.hospital.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

/**
 * UC-26 Εξαγωγή PDF (FR-29, FR-30) με OpenPDF. Γραμματοσειρά DejaVu Sans (ελληνικά), ενσωματωμένη στο αρχείο.
 * Ένα έγγραφο = τίτλος, γραμμές στοιχείων και ένας πίνακας.
 */
@Component
public class PdfExporter {

    private static final String FONT = "/fonts/DejaVuSans.ttf";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BaseFont baseFont;

    public PdfExporter() {
        try (InputStream in = PdfExporter.class.getResourceAsStream(FONT)) {
            if (in == null) {
                throw new IllegalStateException("Λείπει η γραμματοσειρά " + FONT);
            }
            baseFont = BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true,
                                           in.readAllBytes(), null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] export(String title, List<String> details, List<String> headers, List<List<String>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph(title, new Font(baseFont, 16, Font.BOLD)));
        Font text = new Font(baseFont, 10);
        for (String line : details) {
            document.add(new Paragraph(line, text));
        }
        document.add(new Paragraph(" ", text));
        document.add(table(headers, rows));
        document.add(new Paragraph("Εκδόθηκε " + LocalDateTime.now().format(STAMP)
            + " · Πληροφοριακό Σύστημα Νοσοκομείου", new Font(baseFont, 8)));
        document.close();
        return out.toByteArray();
    }

    private PdfPTable table(List<String> headers, List<List<String>> rows) {
        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        Font bold = new Font(baseFont, 9, Font.BOLD);
        Font text = new Font(baseFont, 9);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, bold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
        for (List<String> row : rows) {
            for (String value : row) {
                table.addCell(new Phrase(value == null ? "" : value, text));
            }
        }
        return table;
    }
}
