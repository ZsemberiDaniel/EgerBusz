package com.zsemberidaniel.egerbuszuj.interfaces.presenters

import android.content.Context
import com.zsemberidaniel.egerbuszuj.adapters.ChooseStopAdapter
import java.util.HashMap
import java.util.TreeSet

/**
 * Created by zsemberi.daniel on 2017. 05. 31..
 */
interface IChooseStopPresenter : IBasicPresenter {
    fun getNewHeaders(): HashMap<Char, ChooseStopAdapter.ChooseStopHeader>
    fun convertToChooseStopItems(headers: HashMap<Char, ChooseStopAdapter.ChooseStopHeader>):
            TreeSet<ChooseStopAdapter.ChooseStopItem>

    fun getAdapterItemsCopy(): MutableList<ChooseStopAdapter.ChooseStopItem>
    fun stopClicked(context: Context, item: ChooseStopAdapter.ChooseStopItem)
}