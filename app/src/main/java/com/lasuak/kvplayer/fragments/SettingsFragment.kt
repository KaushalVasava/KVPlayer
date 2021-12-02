package com.lasuak.kvplayer.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.lasuak.kvplayer.BuildConfig
import com.lasuak.kvplayer.MainActivity.Companion.themeSelectedId
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.SettingAdapter
import com.lasuak.kvplayer.adapter.SettingListener
import com.lasuak.kvplayer.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment(R.layout.fragment_settings), SettingListener {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var vGroup: ViewGroup
    private lateinit var adapter: SettingAdapter

    companion object {
        private var settingList = ArrayList<Setting>()
        var theme = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        vGroup = container!!

        binding.settingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        settingList.clear()
        settingList.add(Setting("Appearance", "Choose light or dark theme "))
//        settingList.add(Setting("Feedback", "Provide your feedback regarding to this app"))
//        settingList.add(Setting("Share App", "Share this app with your friends"))
        settingList.add(Setting("Version Number", "Version No: ${BuildConfig.VERSION_NAME}"))

        adapter = SettingAdapter(requireContext(), settingList, this)
        binding.settingRecyclerView.adapter = adapter

        return binding.root
    }

    private fun setTheme(container: ViewGroup?) {
        val themeDialog = LayoutInflater.from(requireContext())
            .inflate(R.layout.theme_dialog, container, false)
        val builder = MaterialAlertDialogBuilder(requireContext())
        val storeTheme = 0

        val themeSelection = themeDialog.findViewById<MaterialButtonToggleGroup>(R.id.selectTheme)

        //checking selected theme
        themeSelection.check(themeSelectedId)

        themeSelection.addOnButtonCheckedListener { group, selectedId, isChecked ->
            if (isChecked) {
                theme = when (selectedId) {
                   // R.id.defaultTheme -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM //system theme
                    R.id.nightTheme -> AppCompatDelegate.MODE_NIGHT_YES //dark theme
                    R.id.lightTheme -> AppCompatDelegate.MODE_NIGHT_NO //light theme
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                themeSelectedId = selectedId
            }
        }


        builder.setView(themeDialog)
            .setTitle("Select Theme")
            .setPositiveButton("Apply") { dialog, _ ->
                requireActivity().setTheme(R.style.Theme_KVPlayer)
                AppCompatDelegate.setDefaultNightMode(theme)

                settingList.removeAt(0)
                settingList.add(0,Setting("Appearance", "Choose your theme and color of theme"))

                adapter.notifyDataSetChanged()
                val editor =
                    requireActivity().getSharedPreferences("THEME", MODE_PRIVATE).edit()
                editor.putInt("theme", themeSelectedId)
//                editor.putInt("colorNo", themeSelectedId)
                editor.apply()
                dialog.dismiss()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    class Setting {
        var headerName: String? = null
        var subName: String? = null

        constructor()
        constructor(headerName: String, subName: String) {
            this.headerName = headerName
            this.subName = subName
        }
    }

    override fun onSettingClicked(position: Int) {
        when (position) {
            0 -> {
                setTheme(vGroup)
            }
//            1 -> {
//                val action =
//                    SettingsDirections.actionAllSettingsToFeedbackFragment()
//                findNavController().navigate(action)
//            }
//            1 -> {
//                try {
//                    val intent = Intent(Intent.ACTION_SEND)
//                    intent.type = "text/plain"
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share Sangeet App")
//                    val shareMsg =
//                        "https://play.google.com/store/apps/details/id=?" + BuildConfig.APPLICATION_ID + "\n\n"
//                    intent.putExtra(Intent.EXTRA_TEXT, shareMsg)
//                    requireActivity().startActivity(Intent.createChooser(intent, "Share by"))
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        requireContext(),
//                        "Some thing went wrong!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            3 -> {
//            }
        }
    }
}