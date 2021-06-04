import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import model.Currency;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Main extends TelegramLongPollingBot {
    public static ArrayList<Currency> currencies;
    public static boolean checkConvert = false;
    public static boolean checkConvert2 = false;
    public static String data = "";

    public static void main(String[] args) throws IOException {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new Main());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        URL url = new URL("https://cbu.uz/oz/arkhiv-kursov-valyut/json/");
        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        Type type = new TypeToken<ArrayList<Currency>>() {
        }.getType();
        currencies = gson.fromJson(bufferedReader, type);


    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (update.hasMessage()) {
            if (message.getText().equals("/start")) {
                execute(startMethod(message));

            } else if (message.getText().equals("Valyutalar kursi \uD83C\uDFE6")) {
                kursMethod(message);
            } else if (message.getText().equals("Onlayn konvertor \uD83D\uDD01")) {
                convertorMethod(message);// logikasi yozilmagan
            } else if (checkConvert) {
                SendMessage sendMessage12 = new SendMessage();
                sendMessage12.setChatId(message.getChatId()).setReplyToMessageId(message.getMessageId());
                sendMessage12.setParseMode(ParseMode.MARKDOWN);
                StringBuilder result = new StringBuilder();

                double value = Double.parseDouble(message.getText());

                for (Currency currency : currencies) {
                    if (currency.getCcy().equals(data)) {
                        double rate = Double.parseDouble(currency.getRate());
                        result.append("*").append(value).append("*").append("_ ").append(data).append("_").append(" ↔ ").append("*").append(String.format("%.2f", (value * rate))).append("*").append("_").append(" UZS").append("_");
                    }
                }
                sendMessage12.setText(result.toString());
                execute(sendMessage12);

            } else if (checkConvert2) {
                SendMessage sendMessage12 = new SendMessage();
                sendMessage12.setChatId(message.getChatId()).setReplyToMessageId(message.getMessageId());
                sendMessage12.setParseMode(ParseMode.MARKDOWN);
                StringBuilder result = new StringBuilder();

                double value = Double.parseDouble(message.getText());

                for (Currency currency : currencies) {
                    if (currency.getCcy().equals(data)) {
                        double rate = Double.parseDouble(currency.getRate());
                        result.append("*").append(value).append("*").append("_ ").append(" UZS").append("_").append(" ↔ ").append("*").append(String.format("%.2f", (value / rate))).append("*").append("_ ").append(data).append("_");
                    }
                }
                sendMessage12.setText(result.toString());
                execute(sendMessage12);


            }
        } else if (update.hasCallbackQuery()) {

            String data1 = callbackQuery.getData();
            String dataText = callbackQuery.getMessage().getText();

            if (data1.equals("USD") || data1.equals("EUR") || data1.equals("CNY")) {

                CallbackQuery callbackQuery1 = update.getCallbackQuery();
                Message message1 = update.getCallbackQuery().getMessage();
                String data = callbackQuery1.getData();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message1.getChatId()).setReplyToMessageId(message1.getMessageId()).setParseMode(ParseMode.MARKDOWN);
                String res = currencyKurs(data, currencies, message1);
                sendMessage.setText(res);
                execute(sendMessage);
            } else if (data1.equals("first") || data1.equals("second")) {
                SendMessage sendMessage = new SendMessage();
                Message message1 = callbackQuery.getMessage();
                sendMessage.setChatId(message1.getChatId()).setReplyToMessageId(message1.getMessageId()).setParseMode(ParseMode.MARKDOWN);
                convertorButtons(data1, message1);
            } else if (data1.equals("dollar") || data1.equals("euro") || data1.equals("yuan")) {
                checkConvert2 = false;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId()).setReplyToMessageId(callbackQuery.getMessage().getMessageId());
                sendMessage.setParseMode(ParseMode.MARKDOWNV2);
                sendMessage.setText("*Summani kiriting: *");
                execute(sendMessage);
                if (data1.equals("dollar")) {
                    data = "USD";
                } else if (data1.equals("euro")) {
                    data = "EUR";
                } else {
                    data = "CNY";
                }
                checkConvert = true;

            } else if (data1.equals("dollar2") || data1.equals("euro2") || data1.equals("yuan2")) {
                checkConvert = false;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId()).setReplyToMessageId(callbackQuery.getMessage().getMessageId());
                sendMessage.setParseMode(ParseMode.MARKDOWN);
                sendMessage.setText("*Summani kiriting: *");
                execute(sendMessage);
                if (data1.equals("dollar2")) {
                    data = "USD";
                } else if (data1.equals("euro2")) {
                    data = "EUR";
                } else {
                    data = "CNY";
                }
                checkConvert2 = true;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "@online_convertor_bot";
    }

    @Override
    public String getBotToken() {
        return "1845848134:AAFMnRmrUt-Xkt0QGN4Zhk09KjDueafrwww";
    }

    public SendMessage startMethod(Message message) {
        SendMessage sendMessage = new SendMessage().setChatId(message.getChatId()).setReplyToMessageId(message.getMessageId());
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("Valyutalar kursi \uD83C\uDFE6");
        KeyboardButton button2 = new KeyboardButton();
        button2.setText("Onlayn konvertor \uD83D\uDD01");
        row1.add(button1);
        row2.add(button2);
        keyboardRowList.add(row1);
        keyboardRowList.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        sendMessage.setText("*Operatsiyani tanlang* \uD83D\uDC47");
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

      return sendMessage;
    }

    public void kursMethod(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId()).setReplyToMessageId(message.getMessageId());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
        List<InlineKeyboardButton> firstList = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton button1 = new InlineKeyboardButton("USD");
        button1.setCallbackData("USD");
        InlineKeyboardButton button2 = new InlineKeyboardButton("EUR");
        button2.setCallbackData("EUR");
        InlineKeyboardButton button3 = new InlineKeyboardButton("CNY");
        button3.setCallbackData("CNY");


        firstList.add(button1);
        firstList.add(button2);
        firstList.add(button3);

        lists.add(firstList);

        inlineKeyboardMarkup.setKeyboard(lists);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);

        sendMessage.setText("*Valyutani tanlang* \uD83D\uDC47");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void convertorMethod(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId()).setReplyToMessageId(message.getMessageId());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
        List<InlineKeyboardButton> firstList = new ArrayList<InlineKeyboardButton>();
        List<InlineKeyboardButton> secondList = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton button1 = new InlineKeyboardButton("Xorijiy valyutadan so'mga");
        InlineKeyboardButton button2 = new InlineKeyboardButton("So'mdan xorijiy valyutaga");
        button1.setCallbackData("first");
        button2.setCallbackData("second");

        firstList.add(button1);
        secondList.add(button2);

        lists.add(firstList);
        lists.add(secondList);

        inlineKeyboardMarkup.setKeyboard(lists);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);

        sendMessage.setText("*Operatsiyani tanlang* \uD83D\uDC47");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void convertorButtons(String data, Message msg) {
        if (data.equals("first")) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(msg.getChatId()).setMessageId(msg.getMessageId());
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton("USD");
            button1.setCallbackData("dollar");
            InlineKeyboardButton button2 = new InlineKeyboardButton("EUR");
            button2.setCallbackData("euro");
            InlineKeyboardButton button3 = new InlineKeyboardButton("CNY");
            button3.setCallbackData("yuan");

            inlineKeyboardButtons.add(button1);
            inlineKeyboardButtons.add(button2);
            inlineKeyboardButtons.add(button3);

            List<List<InlineKeyboardButton>> lists = new ArrayList<>();
            lists.add(inlineKeyboardButtons);
            inlineKeyboardMarkup.setKeyboard(lists);

            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
            editMessageText.setText("Tanlang \uD83D\uDC47");

            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        } else if (data.equals("second")) {

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(msg.getChatId()).setMessageId(msg.getMessageId());
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton("USD");
            button1.setCallbackData("dollar2");
            InlineKeyboardButton button2 = new InlineKeyboardButton("EUR");
            button2.setCallbackData("euro2");
            InlineKeyboardButton button3 = new InlineKeyboardButton("CNY");
            button3.setCallbackData("yuan2");

            inlineKeyboardButtons.add(button1);
            inlineKeyboardButtons.add(button2);
            inlineKeyboardButtons.add(button3);

            List<List<InlineKeyboardButton>> lists = new ArrayList<>();
            lists.add(inlineKeyboardButtons);
            inlineKeyboardMarkup.setKeyboard(lists);

            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            editMessageText.setText("*Qaysi valyutaga konvertatsiya qilasiz? \uD83D\uDC47*");
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    public String currencyKurs(String string, ArrayList<Currency> currencies, Message message) throws TelegramApiException {

        StringBuilder result = new StringBuilder();
        for (Currency currency : currencies) {
            if (currency.getCcy().equals(string)) {
                double dif = Double.parseDouble(currency.getDiff());
                String difString = "";
                if (dif > 0) {
                    difString += "+" + dif + " ↗ ";
                } else {
                    difString += dif + " ↘ ";
                }
                double rate = Double.parseDouble(currency.getRate());
                result.append("*").append(currency.getCcy()).append(" = ").append(rate).append("*").append("        ").append("_").append(difString).append("_").append("\n").append("_LAST UPDATE:    _").append("*").append(currency.getDate()).append("*");

            }
        }

        return String.valueOf(result);

    }


}
