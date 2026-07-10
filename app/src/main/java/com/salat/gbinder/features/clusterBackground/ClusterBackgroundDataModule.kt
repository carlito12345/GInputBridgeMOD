package com.salat.gbinder.features.clusterBackground

import com.salat.gbinder.adb.domain.repository.AdbRepository
import com.salat.gbinder.features.clusterBackground.data.QnxClusterRepositoryImpl
import com.salat.gbinder.features.clusterBackground.domain.QnxClusterRepository
import com.salat.gbinder.statekeeper.domain.repository.StateKeeperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClusterBackgroundDataModule {

    @Provides
    @Singleton
    fun provideQnxClusterRepository(
        adbRepository: AdbRepository,
        stateKeeper: StateKeeperRepository
    ): QnxClusterRepository =
        QnxClusterRepositoryImpl(adbRepository, stateKeeper)
}
