package ru.skillbranch.skillarticles.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

class ChoseCategoryDialog : DialogFragment() {

    private val viewModel: ArticlesViewModel by activityViewModels()
    private val selectedCategories = mutableListOf<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val categories = arrayOf("one", "two")
        val checked = booleanArrayOf(false, false)
        val adb = AlertDialog.Builder(requireContext())
                .setTitle("Chose category")
                .setPositiveButton("Apply") { _, _ ->
                    viewModel.applyCategories(selectedCategories)
                }
                .setNegativeButton("Reset") { _, _ ->
                    viewModel.applyCategories(emptyList())
                }
                .setMultiChoiceItems(categories, checked) { d, which, isChecked ->

                }

        return adb.create()
    }
}