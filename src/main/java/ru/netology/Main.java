package ru.netology;

import java.util.*;

public class Main {
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final int AMOUNT_ROUTES = 1000;
    private static final int ROUTE_LENGTH = 100;
    private static final String ROUTE_GENERATION_TEXT = "RLRFR";
    private static final char CHARACTER_TO_SEARCH = 'R';

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        // Создаём поток для вывода текущего максимума частот и стартуем его
        Thread threadCurrentMaximum = currentMaximum();
        threadCurrentMaximum.start();

        for (int i = 0; i < AMOUNT_ROUTES; i++) {
            // Создаём новый поток для обработки каждого маршрута, сохраняем его в список и стартуем
            Thread threadCountingRouteFrequencies = countingRouteFrequencies();
            threads.add(threadCountingRouteFrequencies);
            threadCountingRouteFrequencies.start();
            threadCountingRouteFrequencies.join();
        }

        // Зависаем, ждём когда поток, объект которого лежит в thread, завершится
        for (Thread thread : threads) {
            thread.join();
        }
        // Прерываем поток вывода текущего максимума
        threadCurrentMaximum.interrupt();
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static Thread countingRouteFrequencies(){
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

    private static Thread currentMaximum(){
        Runnable taskCurrentMaximum = () -> {
            int maximumFrequency = 0;
            int currentMaximum = 0;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        if (entry.getValue() > currentMaximum) {
                            currentMaximum = entry.getValue();
                            maximumFrequency = entry.getKey();
                        }
                    }
                    System.out.println("Текущий лидер частот: " + maximumFrequency);
                }
            }
        };
        return new Thread(taskCurrentMaximum);
    }
}