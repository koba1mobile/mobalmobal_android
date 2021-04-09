package com.mashup.mobalmobal.ui.donationdetail.presenter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.funin.base.funinbase.base.BaseViewBindingFragment
import com.funin.base.funinbase.extension.rx.observeOnMain
import com.funin.base.funinbase.extension.rx.subscribeWithErrorLogger
import com.mashup.base.image.GlideRequests
import com.mashup.mobalmobal.R
import com.mashup.mobalmobal.databinding.FragmentDetailBinding
import com.mashup.mobalmobal.ui.donationdetail.domain.DonationItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DonationDetailFragment : BaseViewBindingFragment<FragmentDetailBinding>() {
    companion object {
        const val KEY_SELECTED_POST_ID = "key_selected_post_id"
    }

    @Inject
    lateinit var glideRequests: GlideRequests
    private val donationDetailViewModel: DonationDetailViewModel by viewModels()

    override fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDetailBinding {
        return FragmentDetailBinding.inflate(inflater, container, false)
    }

    override fun onBindViewModels() {
        donationDetailViewModel.donatinSubject.observeOnMain()
            .subscribeWithErrorLogger { bindDonation(it) }
            .addToDisposables()

        donationDetailViewModel.backTriggerSubject.observeOnMain()
            .subscribeWithErrorLogger { findNavController().popBackStack() }
            .addToDisposables()
    }

    private fun bindDonation(donation: DonationItem) = with(binding) {
        val donationTitle = getString(
            R.string.donation_detail_title,
            donation.author.nickName,
            donation.productName
        )

        tvDonationTitle.text = SpannableStringBuilder(donationTitle).apply {
            setSpan(
                RelativeSizeSpan(1.15f),
                donationTitle.indexOf(donation.author.nickName),
                donationTitle.indexOf(donation.author.nickName) + donation.author.nickName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.light_yellow)),
                donationTitle.indexOf(donation.productName),
                donationTitle.indexOf(donation.productName) + donation.productName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        glideRequests.load(donation.imageUrl)
            .into(ivProduct)

        glideRequests.load(donation.author.profileUrl)
            .circleCrop()
            .into(ivProfile)

        tvDonationDescription.text = donation.description
        tvGoalPrice.text = getString(R.string.donation_price, donation.goalPrice)
        tvDonationCurrnetPrice.text = getString(R.string.donation_price, donation.currentPrice)
        tvDonationEndDate.text = donation.endAt
        tvDonationPercent.text = getString(R.string.donation_percent, donation.donatePercent)
        progressDonating.progress = donation.donatePercent.toInt()
        tvDonator.text = if (donation.donators.isNotEmpty()) {
            getString(R.string.donation_detail_donator, donation.donators.size)
        } else {
            getString(R.string.donation_detail_donator_empty)
        }
        tvFunding.text = getString(R.string.donation_detail_funding, donation.author.nickName)
        vDonator.apply {
            setDonatorProfiles(
                glideRequests,
                donation.donators.map { it.profileUrl }
            )
        }
        tvDonationDDay.text = donation.dueDateText
    }

    private fun navigateDetailToDonate() =
        findNavController().navigate(R.id.action_detailFragment_to_donateFragment)
}