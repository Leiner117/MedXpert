package com.tec.medxpert.ui.login;

import androidx.lifecycle.ViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.data.repository.Authentication;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final Authentication authentication;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final BehaviorSubject<String> loginStatusSubject = BehaviorSubject.create();
    @Inject
    public LoginViewModel(Authentication authentication) {

        this.authentication = authentication;
    }
    public BehaviorSubject<String> getLoginStatusSubject() {
        return loginStatusSubject;
    }
    public void loginWithEmail(String email, String password, AuthenticationNavigator navigator, boolean rememberMe) {
        disposables.add(
            authentication.loginWithEmailRx(email, password, rememberMe)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                isRegistered -> {
                    if (isRegistered) {
                        loginStatusSubject.onNext("login_success");
                        fetchUserRoleAndNavigate(navigator);
                    } else {
                        navigator.navigateToRegister();
                    }
                },
                error -> loginStatusSubject.onNext("Error: " + error.getMessage())
            )
        );
    }

    public void loginWithGoogle(GoogleSignInAccount account, AuthenticationNavigator navigator, boolean rememberMe) {
        disposables.add(
            authentication.loginWithGoogleRx(account, rememberMe)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                isRegistered -> {

                    if (isRegistered) {
                        loginStatusSubject.onNext("login_success");
                        fetchUserRoleAndNavigate(navigator);
                    } else {
                        loginStatusSubject.onNext("account_not_registered");
                        navigator.navigateToRegister();
                    }
                },
                error -> {
                    if ("error_registered_with_email".equals(error.getMessage())) {
                        loginStatusSubject.onNext("account_registered_with_email");
                    } else {
                        loginStatusSubject.onNext("Error: " + error.getMessage());
                    }
                }
            )
        );
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void fetchUserRoleAndNavigate(AuthenticationNavigator navigator) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            loginStatusSubject.onNext("user_not_found");
            return;
        }

        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        if ("doctor".equalsIgnoreCase(role)) {
                            navigator.navigateToHomeDoctor();
                        } else {
                            navigator.navigateToHomePatient();
                        }
                    } else {
                        loginStatusSubject.onNext("user_document_not_found");
                    }
                })
                .addOnFailureListener(e -> loginStatusSubject.onNext("Error: " + e.getMessage()));
    }





    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

}