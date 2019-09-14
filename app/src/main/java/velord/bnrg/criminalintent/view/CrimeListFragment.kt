package velord.bnrg.criminalintent.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
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
import velord.bnrg.criminalintent.viewModel.CrimeListViewModel
import java.util.*


private val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    //Required interface for hosting activities
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)

        fun onNoOneCrime()

        fun hide()
    }

    private var callbacks: Callbacks? =  null

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter =  CrimeAdapter(emptyList())


    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //let the FragmentManager know that
        // CrimeListFragment needs to receive menu callbacks
        setHasOptionsMenu(true)
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
                    when(crimes.size) {
                        0 -> callbacks?.onNoOneCrime()
                        else ->  {
                            callbacks?.hide()
                            updateUI(crimes.reversed())
                        }
                    }
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        fun newInstance() =  CrimeListFragment()
    }

    private val updateUI: (List<Crime>) -> Unit = { crimes ->
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private val makeContentDescription: (String, String, Boolean)
    -> String = { title, time, solved ->
        "$title, $time, ${ 
        if (solved) getString(R.string.crime_handcuff_icon_description)
        else "crime not solved" }"
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
            callbacks?.onCrimeSelected(crimeId = crime.id)
            Toast.makeText(context, "${crime.title} pressed", Toast.LENGTH_SHORT)
                .show()
        }

        fun bind(crime: Crime) {
            this.crime = crime

            titleTextView.apply {
                text = crime.title
                val isSolved = if (crime.isSolved)
                    getString(R.string.crime_handcuff_icon_description)
                else
                    "crime not Solved"
                contentDescription = (this.text.toString() + isSolved)
            }
            dateTextView.apply {
                // “Monday, Jul 22, 2019.”
                val date = android.text.format.DateFormat.format(
                    "EEEE, MMM dd, yyyy", crime.date
                )
                text = date
                contentDescription = this.text
            }
            solvedImageView.visibility =
                if (crime.isSolved)
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

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) =
            crimes[position].let {
                holder.bind(it)
            }


        override fun getItemViewType(position: Int): Int =
            if (crimes[position].requiresPolice)
                 VIEW_TYPE_REQUIRED_POLICE
            else VIEW_TYPE
    }
}



























