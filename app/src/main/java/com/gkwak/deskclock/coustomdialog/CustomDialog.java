package com.gkwak.deskclock.coustomdialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gkwak.deskclock.R;

import org.w3c.dom.Text;

/**
 * Created by 기현 on 2015-12-13.
 */
public class CustomDialog extends Dialog implements View.OnClickListener {
    private String write_daily_text;
    EditText daily_text;
    TextView daily_text_view;
    Button yes_btn;
    Button no_btn;

    public CustomDialog (Context contex) {
        super(contex);

        // 타이틀바 없애기
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        // 커스텀 다이얼로그레이아웃
        setContentView(R.layout.custom_dialog);

        daily_text = (EditText)findViewById(R.id.dailyText);
        yes_btn = (Button)findViewById(R.id.yes_btn);
        no_btn = (Button)findViewById(R.id.no_btn);

        set(daily_text.getText().toString());
        yes_btn.setOnClickListener(this);

    }

    public void onClick(View view) {
        if (view == yes_btn) {
            set(daily_text.getText().toString());

            dismiss();
        } else {
            dismiss();
        }
    }

    public String get() {
        return write_daily_text;
    }

    public void set(String write_daily_text) {
        this.write_daily_text = write_daily_text;
    }

}
