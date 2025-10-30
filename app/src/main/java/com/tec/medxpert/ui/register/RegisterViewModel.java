package com.tec.medxpert.ui.register;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.tec.medxpert.R;
import com.tec.medxpert.data.repository.Authentication;
import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.navigation.codeSecurity.VerificationCodeNavigator;
import com.tec.medxpert.utils.EmailService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final Authentication authentication;
    private final BehaviorSubject<String> registrationStatus = BehaviorSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final Application application;
    private boolean isVerified = false;

    @Inject
    public RegisterViewModel(Authentication authentication, Application application) {
        this.authentication = authentication;
        this.application = application;
    }

    public Observable<String> getRegistrationStatus() {
        return registrationStatus.hide();
    }

    public void signUpWithEmail(String email, String password, VerificationCodeNavigator navigator) {
        SharedPreferences prefs = application.getSharedPreferences("MedXpertPrefs", Context.MODE_PRIVATE);

        if (!isVerified) {
            String securityCode = generateSecurityCode();
            prefs.edit().putString("SecurityCode", securityCode).apply();
            EmailService.sendEmail(application, email, securityCode);
            navigator.navigateToVerificationCode();
        }

        if(isVerified) {
            disposables.add(
                authentication.signUpWithEmailAndPasswordRx(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    authResult -> {
                        registrationStatus.onNext("registration_successful");
                        navigator.navigateToHomePatient();
                    },
                    error -> registrationStatus.onNext("registration_error: " + error.getMessage())
                )
            );
        }
        else {
            navigator.navigateToVerificationCode();

        }

    }

    public void handleSignInWithGoogle(GoogleSignInAccount account, AuthenticationNavigator navigator) {
        disposables.add(
            authentication.signUpWithGoogleRx(account)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                authResult -> {
                    registrationStatus.onNext("registration_successful");
                    navigator.navigateToHomePatient();
                },
                error -> {
                    if ("error_registered_with_email".equals(error.getMessage())) {
                        registrationStatus.onNext(application.getString(R.string.account_registered_with_email));
                    } else {
                        registrationStatus.onNext("registration_error: " + error.getMessage());
                    }
                }
            )
        );
    }

    private String generateSecurityCode() {
        int code = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    public void resendSecurityCode() {
        if (isVerified) {
            return;
        }
        SharedPreferences prefs = application.getSharedPreferences("MedXpertPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("TempEmail", "");

        if (!email.isEmpty()) {
            String newCode = generateSecurityCode();
            prefs.edit().putString("SecurityCode", newCode).apply();
            EmailService.sendEmail(application, email, newCode);
        }
    }


    public void setCodeVerified(boolean verified) {
        isVerified = verified;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }


}
