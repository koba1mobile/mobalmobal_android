package com.mashup.mobalmobal.ui.donationdetail

import com.funin.base.funinbase.base.BaseViewModel
import com.funin.base.funinbase.extension.rx.subscribeWithErrorLogger
import com.funin.base.funinbase.rx.schedulers.BaseSchedulerProvider
import com.mashup.mobalmobal.dto.DonationDto
import com.mashup.mobalmobal.ui.donationdetail.data.repository.DonationDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class DonationDetailViewModel @Inject constructor(
    schedulerProvider: BaseSchedulerProvider,
    private val donationDetailRepository: DonationDetailRepository
) : BaseViewModel(schedulerProvider){

    private val _donationSubject: PublishSubject<DonationDto> = PublishSubject.create()
    val donatinSubject get() = _donationSubject

    fun requestDonationDetail(donationId: String) {
        donationDetailRepository.getDonationDetail(donationId)
            .toFlowable()
            .subscribeOnIO()
            .doOnSubscribe{}
            .subscribeWithErrorLogger { _donationSubject.onNext(it.data) }
            .addToDisposables()
    }
}