package com.softspector.testtable.data.di

import com.softspector.testtable.data.api.MockTableApi
import com.softspector.testtable.data.api.TableApi
import com.softspector.testtable.data.repository.DefaultTableRowsRepository
import com.softspector.testtable.domain.repository.TableRowsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindTableApi(impl: MockTableApi): TableApi

    // the repository itself is built per table, so only its factory is bound
    @Binds
    fun bindTableRowsRepositoryFactory(
        impl: DefaultTableRowsRepository.Factory,
    ): TableRowsRepository.Factory
}
