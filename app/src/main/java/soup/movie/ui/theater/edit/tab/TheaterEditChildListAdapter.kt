package soup.movie.ui.theater.edit.tab

import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_area_group.view.*
import soup.movie.R
import soup.movie.data.TheaterEditManager.Companion.MAX_ITEMS
import soup.movie.data.helper.getFilterChipLayout
import soup.movie.data.model.AreaGroup
import soup.movie.data.model.Theater
import soup.movie.ui.helper.databinding.DataBindingAdapter
import soup.movie.ui.helper.databinding.DataBindingViewHolder
import soup.movie.util.inflate
import soup.movie.util.showToast

class TheaterEditChildListAdapter(private val listener: Listener) :
        DataBindingAdapter<AreaGroup>() {

    interface Listener {

        fun add(theater: Theater): Boolean

        fun remove(theater: Theater)
    }

    private var selectedIdSet: MutableSet<String> = hashSetOf()

    override fun onBindViewHolder(holder: DataBindingViewHolder<AreaGroup>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.theaterListView.apply {
            removeAllViews()
            getItem(position).theaterList.map { theater ->
                inflate<Chip>(context, theater.getFilterChipLayout()).apply {
                    text = theater.name
                    val isSelected = selectedIdSet.contains(theater.id)
                    isChecked = isSelected
                    isChipIconVisible = isSelected.not()
                    setOnCheckedChangeListener { _, checked ->
                        if (checked) {
                            if (listener.add(theater)) {
                                selectedIdSet.add(theater.id)
                                isChipIconVisible = false
                            } else {
                                isChecked = false
                                context.showToast(context.getString(R.string.theater_select_limit_description, MAX_ITEMS))
                            }
                        } else {
                            listener.remove(theater)
                            selectedIdSet.remove(theater.id)
                            isChipIconVisible = true
                        }
                    }
                }
            }.forEach { addView(it) }
        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_area_group

    fun submitList(list: List<AreaGroup>, selectedIdSet: Set<String>) {
        this.selectedIdSet = selectedIdSet.toMutableSet()
        submitList(list)
    }
}
