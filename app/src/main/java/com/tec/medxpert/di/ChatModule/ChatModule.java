package com.tec.medxpert.di.ChatModule;

import android.content.Context;

import com.tec.medxpert.navigation.chat.ChatCoordinator;
import com.tec.medxpert.navigation.chat.ChatNavigation;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ChatModule {

    @Provides
    public ChatNavigation provideChatNavigation(@ApplicationContext Context context) {
        return new ChatCoordinator(context);
    }
}