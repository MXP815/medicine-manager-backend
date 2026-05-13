package com.medicine.medicine_manager_sysytem.service;

import com.medicine.medicine_manager_sysytem.entity.AiSuggestion;
import java.util.List;

public interface AiSuggestionService {

    List<AiSuggestion> getDashboardSuggestions();

    List<AiSuggestion> getInventoryWarnings();

    List<AiSuggestion> getPurchaseSuggestions();

    List<AiSuggestion> getSalesSuggestions();

    List<AiSuggestion> getSmartPurchaseSuggestions();

    void generateDailySuggestions();
}
