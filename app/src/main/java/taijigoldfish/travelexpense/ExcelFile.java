package taijigoldfish.travelexpense;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public class ExcelFile {
    private static final String SHEET_NAME_MAIN = "Trip";
    private static final String SHEET_NAME_EXPENSE = "Expense";

    private WritableWorkbook workbook;

    private WritableCellFormat timesBoldUnderline;
    private WritableCellFormat times;

    public ExcelFile(OutputStream outputStream) {
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setLocale(Locale.ENGLISH);
        try {
            this.workbook = Workbook.createWorkbook(outputStream);

            WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
            // Define the cell format
            this.times = new WritableCellFormat(times10pt);
            // Lets automatically wrap the cells
            this.times.setWrap(true);

            // create create a bold font with unterlines
            WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false,
                    UnderlineStyle.SINGLE);
            this.timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
            // Lets automatically wrap the cells
            this.timesBoldUnderline.setWrap(true);

        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    public void write(Trip trip) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        WritableSheet main = this.workbook.createSheet(SHEET_NAME_MAIN, 0);
        WritableSheet expense = this.workbook.createSheet(SHEET_NAME_EXPENSE, 1);

        try {
            // write main sheet
            addCaption(main, 0, 1, "Destination");
            addCaption(main, 0, 2, "Start Date");
            addCaption(main, 0, 3, "End Date");
            addCaption(main, 0, 4, "Currency");

            addLabel(main, 1, 1, trip.getDestination());
            addLabel(main, 1, 2, dateFormat.format(trip.getStartDate()));
            addLabel(main, 1, 3, dateFormat.format(trip.getEndDate()));
            addLabel(main, 1, 4, trip.getCurrency());

            // write expense sheet
            addCaption(expense, 0, 0, "Day");
            addCaption(expense, 1, 0, "Item Type");
            addCaption(expense, 2, 0, "Item Desc");
            addCaption(expense, 3, 0, "Pay Type");
            addCaption(expense, 4, 0, "Amount");

            int col = 1;
            for (Integer day : trip.getItemMap().keySet()) {
                List<Item> items = trip.getItemMap().get(day);
                for (Item item : items) {
                    addLabel(expense, 0, col, "" + day);
                    addLabel(expense, 1, col, item.getType());
                    addLabel(expense, 2, col, item.getDetails());
                    addLabel(expense, 3, col, item.getPayType());
                    addLabel(expense, 4, col, "" + item.getAmount());
                    col++;
                }
            }

            this.workbook.write();
            this.workbook.close();

        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws WriteException {
        Label label;
        label = new Label(column, row, s, this.timesBoldUnderline);
        sheet.addCell(label);
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException {
        Label label;
        label = new Label(column, row, s, this.times);
        sheet.addCell(label);
    }

}
