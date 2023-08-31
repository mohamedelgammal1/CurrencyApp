package ui.currencyapp.ui.main

import android.app.Activity
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.ext.android.viewModel
import ui.currencyapp.R
import ui.currencyapp.databinding.FragmentCurrencyBinding
import ui.currencyapp.domain.Currency
import ui.currencyapp.ui.extensions.inflate

class CurrencyFragment : Fragment(), OnChartValueSelectedListener {
    private lateinit var binding: FragmentCurrencyBinding

    private val mOnValueSelectedRectF: RectF = RectF()

    private lateinit var amount: String
    private lateinit var adapter: CurrencyAdapter
    private var currenciesList: MutableList<Currency> = mutableListOf()

    private val viewModel: CurrencyViewModel by currentScope.viewModel(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrencyBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.currencyList.layoutManager = mLayoutManager
        binding.currencyList.setHasFixedSize(true)

        adapter = CurrencyAdapter()
        binding.currencyList.adapter = adapter

        viewModel.model.observe(viewLifecycleOwner, Observer(::updateUi))
    }

    private fun updateUi(model: CurrencyViewModel.UiModel) {

        binding.containerProgressIndicator.visibility =
            if (model is CurrencyViewModel.UiModel.Loading) View.VISIBLE else View.GONE

        when (model) {
            is CurrencyViewModel.UiModel.Content -> {
                currenciesList = adapter.currencies
                adapter.currencies = model.currencies

                amount = binding.amountEditText.text.toString()

                binding.amountEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                    override fun onEditorAction(
                        text: TextView,
                        actionId: Int,
                        event: KeyEvent?
                    ): Boolean {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || actionId == EditorInfo.IME_ACTION_SEND
                            || event?.action == KeyEvent.ACTION_DOWN
                            && event.keyCode == KeyEvent.KEYCODE_ENTER
                        ) {

                            amount = text.text.toString()

                            if (amount.isEmpty()) {
                                return true
                            }

                            setData(
                                MAX_CHART_HORIZONTAL,
                                MAX_RANGE,
                                if (adapter.currencies[0].rates?.eur!! > 0.0) {
                                    adapter.currencies[0].rates?.eur?.times(amount.toInt())
                                } else {
                                    adapter.currencies[0].rates?.eur
                                },
                                if (adapter.currencies[0].rates?.jpy!! > 0.0) {
                                    adapter.currencies[0].rates?.jpy?.times(amount.toInt())
                                } else {
                                    adapter.currencies[0].rates?.jpy
                                },
                                if (adapter.currencies[0].rates?.gbp!! > 0.0) {
                                    adapter.currencies[0].rates?.gbp?.times(amount.toInt())
                                } else {
                                    adapter.currencies[0].rates?.gbp
                                },
                                if (adapter.currencies[0].rates?.brl!! > 0.0) {
                                    adapter.currencies[0].rates?.brl?.times(amount.toInt())
                                } else {
                                    adapter.currencies[0].rates?.brl
                                }
                            )

                            adapter.setAmount(adapter.currencies[0], amount.toInt())
                            hideKeyboard(requireActivity())
                            return true
                        }
                        return true
                    }
                })

                binding.horizontalChart.setOnChartValueSelectedListener(this)
                // chart.setHighlightEnabled(false);

                binding.horizontalChart.setDrawBarShadow(false)

                binding.horizontalChart.setDrawValueAboveBar(true)

                binding.horizontalChart.description.isEnabled = false

                // if more than 60 entries are displayed in the chart, no values will be
                // drawn
                binding.horizontalChart.setMaxVisibleValueCount(60)

                // scaling can now only be done on x- and y-axis separately
                binding.horizontalChart.setPinchZoom(false)

                // draw shadows for each bar that show the maximum value
                // chart.setDrawBarShadow(true);

                binding.horizontalChart.setDrawGridBackground(false)

                val xl: XAxis = binding.horizontalChart.xAxis
                xl.position = XAxis.XAxisPosition.BOTTOM
                xl.setDrawAxisLine(true)
                xl.setDrawGridLines(false)
                xl.granularity = 10f

                val yl: YAxis = binding.horizontalChart.axisLeft
                yl.setDrawAxisLine(true)
                yl.setDrawGridLines(true)
                yl.axisMinimum = 0f

                val yr: YAxis = binding.horizontalChart.axisRight
                yr.setDrawAxisLine(true)
                yr.setDrawGridLines(false)
                yr.axisMinimum = 0f

                binding.horizontalChart.setFitBars(true)
                binding.horizontalChart.animateY(2500)

                val l: Legend = binding.horizontalChart.legend
                l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                l.orientation = Legend.LegendOrientation.HORIZONTAL
                l.setDrawInside(false)
                l.formSize = 8f
                l.xEntrySpace = 4f
            }
            CurrencyViewModel.UiModel.showUI -> {
                viewModel.showUi()
            }

            else -> {}
        }
    }


    private fun setData(
        count: Int,
        range: Float,
        eur: Double?,
        jpy: Double?,
        gbp: Double?,
        brl: Double?
    ) {

        val barWidth = 9f
        val spaceForBar = 10f
        val values: ArrayList<BarEntry> = ArrayList()

        for (pos in 0..count) {
            var value = eur?.times(range)?.toFloat()
            when (pos) {
                0 -> values.add(
                    BarEntry(pos * spaceForBar, value!!, null)
                )
                1 -> {
                    value = jpy?.times(range)?.toFloat()
                    values.add(
                        BarEntry(pos * spaceForBar, value!!, null)
                    )
                }
                2 -> {
                    value = gbp?.times(range)?.toFloat()
                    values.add(
                        BarEntry(pos * spaceForBar, value!!, null)
                    )
                }
                3 -> {
                    value = brl?.times(range)?.toFloat()
                    values.add(
                        BarEntry(pos * spaceForBar, value!!, null)
                    )
                }
            }
        }

        val set1: BarDataSet
        val set2: BarDataSet
        val set3: BarDataSet
        val set4: BarDataSet

        if (binding.horizontalChart.data != null &&
            binding.horizontalChart.data.dataSetCount > 0
        ) {
            set1 = binding.horizontalChart.data.getDataSetByIndex(0) as BarDataSet
            set2 = binding.horizontalChart.data.getDataSetByIndex(1) as BarDataSet
            set3 = binding.horizontalChart.data.getDataSetByIndex(2) as BarDataSet
            set4 = binding.horizontalChart.data.getDataSetByIndex(3) as BarDataSet
            set1.values = values
            set2.values = values
            set3.values = values
            set4.values = values
            binding.horizontalChart.data.notifyDataChanged()
            binding.horizontalChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "EUR")
            set2 = BarDataSet(values, "JPY")
            set3 = BarDataSet(values, "GBP")
            set4 = BarDataSet(values, "BLR")

            set1.setDrawIcons(false)
            set1.setColors(resources.getColor(R.color.darkBlue))
            set2.setColors(resources.getColor(R.color.red))
            set3.setColors(resources.getColor(R.color.blue))
            set4.setColors(resources.getColor(R.color.green))

            val dataSets: ArrayList<IBarDataSet> = ArrayList()
            dataSets.add(set1)
            dataSets.add(set2)
            dataSets.add(set3)
            dataSets.add(set4)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.barWidth = barWidth
            binding.horizontalChart.data = data
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onNothingSelected() {

    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null)
            return

        val bounds: RectF = mOnValueSelectedRectF
        binding.horizontalChart.getBarBounds((e as BarEntry), bounds)

        val position: MPPointF = binding.horizontalChart.getPosition(e,
            h?.dataSetIndex?.let { binding.horizontalChart.data.getDataSetByIndex(it).axisDependency })

        Log.i("bounds", bounds.toString())
        Log.i("position", position.toString())

        MPPointF.recycleInstance(position)
    }

    companion object {
        private val MAX_CHART_HORIZONTAL = 3
        private val MAX_RANGE = 100F
    }
}