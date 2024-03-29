package com.ensb.vendviteexpress.view.ui.distributor.home

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ensb.vendviteexpress.databinding.FragmentSellerInfoDialogListDialogBinding
import com.ensb.vendviteexpress.utils.TAG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale


class SellerInfoDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSellerInfoDialogListDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSellerInfoDialogListDialogBinding.inflate(inflater, container, false)


        val sellerId = arguments?.getString("sellerId")
        fetchSellerInfoFromFirebase(sellerId.toString())

        binding.btnCallSeller.setOnClickListener {
            Snackbar.make(
                requireView(),
                "This feature is coming soon..",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("DEPRECATION")
    private fun getReadableAddressFromGeoPoint(
        context: Context,
        geoPoint: GeoPoint
    ): String? {
        val geocoder = Geocoder(context, Locale.FRENCH)
        return try {
            val addresses: List<Address>? = geocoder.getFromLocationName(
                "${geoPoint.latitude},${geoPoint.longitude}",
                1
            )
            addresses?.getOrNull(0)?.getAddressLine(0)
        } catch (e: IOException) {
            Log.e("Geocoding", "Error retrieving address:", e)
            null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchSellerInfoFromFirebase(distributorId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val snapshot =
                    Firebase.firestore.collection("users").document(distributorId).get().await()
                val geoPoint = snapshot.getGeoPoint("location")
                val name = snapshot.getString("name")
                val type = snapshot.getString("type")

                if (geoPoint != null) {
                    GlobalScope.launch(Dispatchers.Main) {


                        binding.textView2.text = name
                        binding.textView3.text =
                            getReadableAddressFromGeoPoint(requireContext(), geoPoint)
                    }
                } else {
                    Toast.makeText(requireContext(), "geoPoint not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

}