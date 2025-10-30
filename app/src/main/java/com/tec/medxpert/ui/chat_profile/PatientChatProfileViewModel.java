package com.tec.medxpert.ui.chat_profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tec.medxpert.data.model.patientChats.ProfileChat;
import com.tec.medxpert.data.repository.profileChatPatient.ProfileChatRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PatientChatProfileViewModel extends ViewModel {
    private final ProfileChatRepository repository;
    private final MutableLiveData<ProfileChat> profileChatLiveData = new MutableLiveData<>();

    @Inject
    public PatientChatProfileViewModel(ProfileChatRepository repository) {
        this.repository = repository;
    }

    public LiveData<ProfileChat> getProfileChatByUserId(String userId) {
        repository.getProfileChatByUserId(userId, new ProfileChatRepository.OnProfileChatLoadedCallback() {
            @Override
            public void onSuccess(List<ProfileChat> profileChats) {
                if (!profileChats.isEmpty()) {
                    profileChatLiveData.postValue(profileChats.get(0));
                } else {
                    profileChatLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                profileChatLiveData.postValue(null);
            }
        });
        return profileChatLiveData;
    }
}