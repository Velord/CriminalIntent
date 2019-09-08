package velord.bnrg.criminalintent.viewModel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.model.Crime





private val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter =  CrimeAdapter(emptyList())


    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_crime_list, container, false)
        findViews(view)
        initViewsEvents()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    private val initViewsEvents = {
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
    }

    private val findViews: (View) -> Unit = { view ->
        crimeRecyclerView =
            view.findViewById(R.id.crime_recycler_view) as RecyclerView
    }

    companion object {
        val newInstance = {
            CrimeListFragment()
        }
    }

    private val updateUI: (List<Crime>) -> Unit = { crimes ->
        adapter = CrimeAdapter(crimes)
    }

    private inner class CrimeHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            Toast.makeText(context, "${crime.title} pressed", Toast.LENGTH_SHORT)
                .show()
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            // “Monday, Jul 22, 2019.”
            val date = android.text.format.DateFormat.format(
                "EEEE, MMM dd, yyyy", this.crime.date)
            dateTextView.text = date
            solvedImageView.visibility = if (crime.isSolved)
                View.VISIBLE
            else
                View.GONE
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>() {

        private val VIEW_TYPE = 0
        private val VIEW_TYPE_REQUIRED_POLICE = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = if (viewType == VIEW_TYPE)
                layoutInflater.inflate(R.layout.list_item_crime, parent,
                    false)
            else
                layoutInflater.inflate(R.layout.list_item_crime_required_police,
                    parent, false)

            return CrimeHolder(view)
        }

        override fun getItemCount(): Int = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemViewType(position: Int): Int {
            if (crimes[position].requiresPolice)
                return VIEW_TYPE_REQUIRED_POLICE
            return VIEW_TYPE
        }
    }
}



























