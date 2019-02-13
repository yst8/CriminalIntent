package local.uri.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crimeId";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_READ_CONTACTS = 3;
    private static final int REQUEST_PHOTO = 4;


    private Crime mCrime;
    private EditText mTitleField;
    private EditText mDescField;
    private Button mDataButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mRemoveCrime;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspect;
    private String mContactId;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        if (mCrime.getSuspectId() != null) {
            mContactId = mCrime.getSuspectId();
            Log.i("TAGTAGTAG", "FROMLAUNCH contactIId:" + mContactId);

        }

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDescField = (EditText) v.findViewById(R.id.crime_details);
        mDescField.setText(mCrime.getDescription());
        mDescField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setDescription(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDataButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        //mDataButton.setEnabled(false);
        mDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerfragment dialog = DatePickerfragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.setTimeButton);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(fragmentManager, DIALOG_TIME);
            }
        });

        mRemoveCrime = v.findViewById(R.id.remove_crime_button);
        mRemoveCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrimeLab.get(getActivity()).removeCrime(mCrime);
                getActivity().finish();
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCrime.setSolved(b);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);*/

                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report)
                        .createChooserIntent();

                startActivity(i);
            }
        });

        final Intent pickContract = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        //pickContract.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContract, REQUEST_CONTACT);
            }
        });

        mCallSuspect = v.findViewById(R.id.makeCall);
        mCallSuspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permissionStatus = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);

                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                } else {
                    makeCall();
                }
            }
        });

        if (mCrime.getSuspect() == null) {
            mCallSuspect.setEnabled(false);
        } else {
            mSuspectButton.setText(mCrime.getSuspect());
            mCallSuspect.setEnabled(true);
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContract, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            //Uri uri = Uri.fromFile(mPhotoFile);
            Uri uri = FileProvider.getUriForFile(getActivity(), getString(R.string.file_provider_authority), mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mPhotoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mPhotoFile.exists()) {
                    return;
                }
                //Toast.makeText(getContext(), "ViewPressed!", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.DialogFragment dialogFragment = OpenThePhotoFragment.newInstance(mPhotoFile.getPath());
                dialogFragment.show(getFragmentManager(), "PhotoDialog");
            }
        });



        return v;
    }

    private void updateDate() {
        mDataButton.setText(DateFormat.format("dd-MM-yy, HH:mm:ss, EEE", mCrime.getDate()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerfragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }

        if (requestCode == REQUEST_TIME) {
            int[] time = data.getIntArrayExtra(TimePickerFragment.EXTRA_TIME);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mCrime.getDate());
            calendar.set(Calendar.HOUR_OF_DAY, time[0]);
            calendar.set(Calendar.MINUTE, time[1]);
            mCrime.setDate(calendar.getTime());
            updateDate();
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
            Cursor cc = getActivity().getContentResolver().query(contactUri, null, null, null, null);

            try {
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();


                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                mCallSuspect.setEnabled(true);

                if (cc.getCount() > 0) {
                    cc.moveToFirst();
                    mContactId = cc.getString(cc.getColumnIndex(ContactsContract.Contacts._ID));
                    mCrime.setSuspectId(mContactId);
                }
            } finally {
                c.close();
                cc.close();
            }
        }

        if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    private String getCrimeReport() {
        String solvedString = null;

        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                } else {
                    Toast.makeText(super.getActivity().getApplicationContext(), "Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }*/

    private void makeCall() {
        Cursor c = getActivity().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId,
                        null, null);
        c.moveToFirst();
        Log.i("TAGTAGTAG", "size:" + Integer.toString(c.getCount()));
        Log.i("TAGTAGTAG", "contactIId:" + mContactId);
        String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        c.close();

        Uri dialNumber = Uri.parse("tel:" + number);
        Intent i = new Intent(Intent.ACTION_DIAL, dialNumber);
        startActivity(i);
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

}