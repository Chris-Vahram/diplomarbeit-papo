package at.papo.partnerportal.export;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderStatus;
import com.opencsv.CSVWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// German-locale Excel (Austrian client) expects ';' as separator and a UTF-8 BOM
// to render ä/ö/ü/ß correctly instead of mojibake - mirrors the encoding rules
// the CSV importer already applies for the opposite direction (data-export spec).
@ApplicationScoped
public class CsvExportService {

    private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private static final Map<OrderStatus, String> STATUS_LABELS = Map.of(
            OrderStatus.SUBMITTED, "Eingereicht",
            OrderStatus.REVIEWED, "Geprüft",
            OrderStatus.FULFILLED, "Abgeschlossen"
    );

    @Inject
    CommissionService commissionService;

    public byte[] exportOrders() {
        List<Order> orders = Order.listAll();
        Map<Long, CommissionRecord> commissionsByOrderId = commissionService.findForOrders(orders);

        return writeCsv(new String[] { "Datum", "Partner", "Kunde", "Wert", "Provision %", "Provision", "Status" },
                orders, order -> {
                    CommissionRecord commission = commissionsByOrderId.get(order.id);
                    return new String[] {
                            order.orderDate.toString(),
                            order.partner.name,
                            kundeName(order),
                            order.orderValue.toString(),
                            commission != null ? commission.commissionPercentUsed.toString() : "",
                            commission != null ? commission.commissionValue.toString() : "",
                            STATUS_LABELS.get(order.status),
                    };
                });
    }

    // Only orders with a commission record - settlement-ready sheet, not an order log (design.md).
    public byte[] exportCommissions() {
        List<CommissionRecord> records = CommissionRecord.listAll();

        return writeCsv(new String[] { "Partner", "Bestell-Nr", "Basiswert", "Provision %", "Provision" },
                records, record -> new String[] {
                        record.partner.name,
                        String.valueOf(record.order.id),
                        record.orderValue.toString(),
                        record.commissionPercentUsed.toString(),
                        record.commissionValue.toString(),
                });
    }

    private static String kundeName(Order order) {
        String firstName = order.kunde.firstName;
        return (firstName != null && !firstName.isBlank() ? firstName + " " : "") + order.kunde.lastName;
    }

    private <T> byte[] writeCsv(String[] header, List<T> rows, Function<T, String[]> toRow) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(UTF8_BOM);
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
                writer.writeNext(header);
                for (T row : rows) {
                    writer.writeNext(toRow.apply(row));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
