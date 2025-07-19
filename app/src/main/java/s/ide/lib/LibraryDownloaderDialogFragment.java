package s.ide.lib;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.Objects;

import s.ide.databinding.LibraryDownloaderDialogBinding;


public class LibraryDownloaderDialogFragment extends BottomSheetDialogFragment {
    private LibraryDownloaderDialogBinding binding;
    private LibraryDownloaderListener listener;

    public void setLibraryDownloaderListener(LibraryDownloaderListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LibraryDownloaderDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnDownload.setOnClickListener(v -> {
            String dependency = Objects.requireNonNull(binding.dependencyInput.getText()).toString();
            boolean skipSubDependencies = binding.cbSkipSubdependencies.isChecked();

            if (listener != null) {
                try {
                    listener.onDownloadRequested(dependency, skipSubDependencies);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            dismiss();
        });
    }
}
