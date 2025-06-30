package com.jgdigital.ocr

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.jgdigital.ocr.R

class ExpandableListAdapter(
    private val context: Context,
    private val listDataHeader: List<String>,
    private val listChildData: HashMap<String, List<String>>
) : BaseExpandableListAdapter() {
    override fun getChild(groupPosition: Int, childPosititon: Int): Any {
        return listChildData[listDataHeader[groupPosition]]!![childPosititon]
    }
    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }
    override fun getChildrenCount(groupPosition: Int): Int {
        return listChildData[listDataHeader[groupPosition]]!!.size
    }
    override fun getGroup(groupPosition: Int): Any {
        return listDataHeader[groupPosition]
    }
    override fun getGroupCount(): Int {
        return listDataHeader.size
    }
    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }
    override fun hasStableIds(): Boolean {
        return false
    }
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val headerTitle = getGroup(groupPosition) as String
        if (cv == null) {
            val infalInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = infalInflater.inflate(R.layout.list_group, null)
        }
        val lblListHeader = cv!!.findViewById<TextView>(R.id.lblListHeader)
        lblListHeader.text = headerTitle
        return cv
    }
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val childText = getChild(groupPosition, childPosition) as String
        if (cv == null) {
            val infalInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = infalInflater.inflate(R.layout.list_item, null)
        }
        val txtListChild = cv!!.findViewById<TextView>(R.id.lblListItem)
        txtListChild.text = childText
        return cv
    }
}
