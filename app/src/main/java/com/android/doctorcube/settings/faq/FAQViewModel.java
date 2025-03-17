package com.android.doctorcube.settings.faq;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class FAQViewModel extends ViewModel {
    private MutableLiveData<List<FAQItem>> faqList = new MutableLiveData<>();

    public LiveData<List<FAQItem>> getFaqList() {
        return faqList;
    }

    public FAQViewModel() {
        loadFAQs();
    }

    private void loadFAQs() {
        List<FAQItem> faqs = new ArrayList<>();
        faqs.add(new FAQItem("How do I reset my password?", "Go to settings and select 'Reset Password'."));
        faqs.add(new FAQItem("Can I change my username?", "No, usernames cannot be changed after registration."));
        faqs.add(new FAQItem("Is my data secure?", "Yes, we use AES-256 encryption for all stored data."));
        faqs.add(new FAQItem("How do I contact support?", "You can contact support via email at support@example.com."));
        faqs.add(new FAQItem("How to delete my account?", "Go to settings > Account > Delete Account."));

        faqList.setValue(faqs);
    }
}

