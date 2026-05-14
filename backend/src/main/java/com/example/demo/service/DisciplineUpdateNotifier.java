package com.example.demo.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DisciplineUpdateNotifier {
    private final Map<Long, DeferredResult<Boolean>> waitingClients = new ConcurrentHashMap<>();

    public DeferredResult<Boolean> registerListener(Long disciplineId) {
        DeferredResult<Boolean> output = new DeferredResult<>(60000L, false); // таймаут 60 сек
        waitingClients.put(disciplineId, output);

        output.onCompletion(() -> waitingClients.remove(disciplineId));
        output.onTimeout(() -> waitingClients.remove(disciplineId));

        return output;
    }

    public void notifyListeners(Long disciplineId) {
        DeferredResult<Boolean> result = waitingClients.get(disciplineId);
        if (result != null) {
            result.setResult(true);
        }
    }
}
