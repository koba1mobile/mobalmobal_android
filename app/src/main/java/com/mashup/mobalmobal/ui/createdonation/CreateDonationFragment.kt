package com.mashup.mobalmobal.ui.createdonation

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.core.animation.doOnEnd
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.funin.base.funinbase.base.BaseViewBindingFragment
import com.funin.base.funinbase.extension.showSoftInput
import com.mashup.base.image.GlideRequests
import com.mashup.mobalmobal.databinding.FragmentCreateDonationBinding
import dagger.hilt.android.AndroidEntryPoint
import gun0912.tedimagepicker.builder.TedRxImagePicker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.min


@AndroidEntryPoint
class CreateDonationFragment : BaseViewBindingFragment<FragmentCreateDonationBinding>() {
    @Inject
    lateinit var glideRequest: GlideRequests
    private val createDonationViewModel by activityViewModels<CreateDonationViewModel>()

    private var createDonationProductNameWatcher: TextWatcher? = null
    private var createDonationDescriptionWatcher: TextWatcher? = null
    private var createDonationPriceWatcher: TextWatcher? = null
    private var result = ""

    companion object {
        private const val START_PICKER_ID = 1
        private const val END_PICKER_ID = 2
        private const val DAY_DIFF = 7
        private const val HOUR_DIFF = 1
    }

    override fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateDonationBinding =
        FragmentCreateDonationBinding.inflate(inflater, container, false)

    override fun onSetupViews(view: View) {
        setActionListenerAtCreateDonationInputs()
        setImageFromGallery()
        setInputTextWatcher()
        setDateTimePickerDialog()
    }

    private fun setInputTextWatcher() {
        createDonationProductNameWatcher =
            binding.createDonationNameInput.doOnTextChanged { text, _, _, _ ->
                createDonationViewModel.setCreateDonationProductName(text.toString())
            }

        createDonationDescriptionWatcher =
            binding.createDonationDescriptionInput.doOnTextChanged { text, _, _, _ ->
                createDonationViewModel.setCreateDonationDescription(text.toString())
            }

        createDonationPriceWatcher =
            binding.createDonationPriceInput.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isNotBlank() && text.toString() != result) {
                    result =
                        String.format("%,d", text.toString().replace(",", "").toLongOrNull() ?: 0L)
                    binding.createDonationPriceInput.setText(result)
                    binding.createDonationPriceInput.setSelection(result.length)
                }
            }
    }

    private fun setImageFromGallery() {
        binding.createDonationProductImageView.setOnClickListener {
            TedRxImagePicker.with(requireActivity())
                .start()
                .subscribe({ uri ->
                    createDonationViewModel.setCreateDonationFile(uri)
                    glideRequest.load(uri)
                        .centerCrop()
                        .into(binding.createDonationProductImageView)
                }, { error ->
                    Log.e("CreateDonationShow", "Set image from gallery", error)
                })
        }
    }

    private fun setActionListenerAtCreateDonationInputs() {
        binding.createDonationNameInput.setAnimationListener(
            EditorInfo.IME_ACTION_DONE,
            0,
            binding.createDonationDescriptionTextInputLayout,
            binding.createDonationDescriptionInput
        )

        binding.createDonationDescriptionInput.setAnimationListener(
            EditorInfo.IME_ACTION_DONE,
            1,
            binding.createDonationPriceTextInputLayout,
            binding.createDonationPriceInput
        )

        binding.createDonationPriceInput.setAnimationListener(
            EditorInfo.IME_ACTION_DONE,
            2,
            binding.createDonationStartDateInputLayout,
            null
        )

    }

    private fun EditText.setAnimationListener(
        imeActionId: Int,
        viewIndex: Int,
        nextViewLayout: View,
        nextView: View?
    ) {
        setOnEditorActionListener { v, actionId, event ->
            if (actionId == imeActionId && binding.createDonationProductImageView.visibility != View.VISIBLE) {
                goDownAnimation(viewIndex, nextViewLayout, nextView)
            }
            false
        }
    }

    private fun goDownAnimation(
        viewIndex: Int,
        nextViewLayout: View,
        nextView: View?
    ) {
        ObjectAnimator.ofFloat(
            binding.createDonationInputWrapperLayout,
            "translationY",
            binding.createDonationNameTextInputLayout.height.toFloat() * (viewIndex + 1)
        ).apply {
            duration = 300
            start()
        }.doOnEnd {
            nextViewLayout.visibility = View.VISIBLE
            nextView?.requestFocus()
            (nextView as? EditText)?.showSoftInput()
        }
    }

    private fun setDateTimePickerDialog() {
        binding.createDonationStartDateInput.setOnClickListener {
            showDatePickerDialog(START_PICKER_ID)
        }
        binding.createDonationDueDateInput.setOnClickListener {
            showDatePickerDialog(END_PICKER_ID)
        }
    }

    private fun showDatePickerDialog(viewId: Int) {
        val c = Calendar.getInstance()
        if (viewId == 2) {
            c.add(Calendar.DAY_OF_MONTH, DAY_DIFF)
        }
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val sumOfDate = year + month + dayOfMonth
            showTimePickerDialog(viewId, sumOfDate, year, month, dayOfMonth)
        }

        DatePickerDialog(requireActivity(), dateSetListener, year, month, day).show()
    }

    private fun showTimePickerDialog(viewId: Int, sumOfDate: Int, year: Int, month: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        if (viewId == 2) {
            c.add(Calendar.DAY_OF_MONTH, DAY_DIFF)
            c.add(Calendar.HOUR_OF_DAY, HOUR_DIFF)
        }
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            val calendar = Calendar.getInstance().also {
                it.set(Calendar.YEAR, year)
                it.set(Calendar.MONTH, month)
                it.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                it.set(Calendar.HOUR_OF_DAY, hourOfDay)
                it.set(Calendar.MINUTE, minute)
            }
            val formattedDateTime = getFormattedDateTime(calendar.timeInMillis)

            if (viewId == 1) {
                createDonationViewModel.setCreateDonationStartDate(calendar.timeInMillis)
                binding.createDonationStartDateInput.setText(formattedDateTime)
                goDownAnimation(3,
                    binding.createDonationDueDateInputLayout,
                    null
                )
            } else {
                createDonationViewModel.setCreateDonationDueDate(calendar.timeInMillis)
                binding.createDonationDueDateInput.setText(formattedDateTime)
                goDownAnimation(4,
                    binding.createDonationProductImageView,
                    null
                )
            }
        }
        TimePickerDialog(activity, timeSetListener, hour, minute, DateFormat.is24HourFormat(activity)).show()
    }

    private fun getFormattedDateTime(milliSeconds: Long): String{
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(milliSeconds)
    }

}


