package com.peluqueriacanina.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.codetrixstudio.capacitor.GoogleAuth.GoogleAuth;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Registrar el plugin de Google Auth (Modo Nativo)
        registerPlugin(GoogleAuth.class);
    }
}
