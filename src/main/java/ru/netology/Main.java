package ru.netology;

import java.util.*;

public class Main {
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final int AMOUNT_ROUTES = 1000;
    private static final int ROUTE_LENGTH = 100;
    private static final String ROUTE_GENERATION_TEXT = "RLRFR";
    private static final char CHARACTER_TO_SEARCH = 'R';
    private static int maxMeetingFrequency = 0;
    private static int maximumNumberRepetitions = 0;

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < AMOUNT_ROUTES; i++) {
            // Создаём новый поток для обработки каждого маршрута, сохраняем его в список и стартуем
            Thread threadCountingRouteFrequencies = countingRouteFrequencies();
            threads.add(threadCountingRouteFrequencies);
            threadCountingRouteFrequencies.start();
        }

        // Зависаем, ждём когда поток, объект которого лежит в thread, завершится
        for (Thread thread : threads) {
            thread.join();
        }

        // Находим максимальную частоту повторов, сохагяем удаляем её по ключу
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (entry.getValue() > maximumNumberRepetitions) {
                maxMeetingFrequency = key;
                maximumNumberRepetitions = value;
            }
        }
        sizeToFreq.remove(maxMeetingFrequency);

        System.out.println("\nСамое частое количество повторений " + maxMeetingFrequency +
                " (встретилось " + maximumNumberRepetitions + " раз)");

        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static Thread countingRouteFrequencies() {
        Runnable taskCountingRouteFrequencies = () -> {
            // Генерация маршрута
            String route = generateRoute(ROUTE_GENERATION_TEXT, ROUTE_LENGTH);
            // Подсчёт поворотов направо в маршруте
            int numberCoincidences = (int) route.chars()
                    .filter(c -> (c == CHARACTER_TO_SEARCH))
                    .count();
            // Заполняем Map
            synchronized (sizeToFreq) {
                if (sizeToFreq.containsKey(numberCoincidences)) {
                    int lineCounter = sizeToFreq.get(numberCoincidences) + 1;
                    sizeToFreq.put(numberCoincidences, lineCounter);
                } else {
                    sizeToFreq.put(numberCoincidences, 1);
                }
                sizeToFreq.notify();
            }
        };
        return new Thread(taskCountingRouteFrequencies);
    }
}