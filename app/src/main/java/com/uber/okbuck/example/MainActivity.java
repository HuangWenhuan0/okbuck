package com.uber.okbuck.example;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.piasy.rxscreenshotdetector.RxScreenshotDetector;
import com.promegu.xlog.base.XLog;
import com.uber.okbuck.example.common.Calc;
import com.uber.okbuck.example.common.CalcMonitor;
import com.uber.okbuck.example.common.IMyAidlInterface;
import com.uber.okbuck.example.dummylibrary.DummyActivity;
import com.uber.okbuck.example.dummylibrary.DummyAndroidClass;
import com.uber.okbuck.example.javalib.DummyJavaClass;
import com.uber.okbuck.example.sqldelightmodel.GithubRepo;
import com.uber.okbuck.example.sqldelightmodel.GithubUser;
import com.uber.okbuck.java.Pojo;
import com.uber.okbuck.kotlin.KotlinDataClass;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@XLog
public class MainActivity extends AppCompatActivity {
    @Inject
    DummyJavaClass mDummyJavaClass;
    @Inject
    DummyAndroidClass mDummyAndroidClass;
    IMyAidlInterface mIMyAidlInterface;

    private Unbinder mUnbinder;

    TextView mTextView;
    TextView mTextView2;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIMyAidlInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RibletViewProvider.Factory factory = hostRouter -> new RibletViewProvider(hostRouter) {
          @Override
          public void buildViewRouter() {
              System.out.println(this.hostRouter);
          }
        };

        Router router = new Router(7);
        new Transaction.DynamicBuilder(factory).build(router);

        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.mTextView);
        mTextView2 = findViewById(R.id.mTextView2);
        mUnbinder = ButterKnife.bind(this);

        View view = findViewById(android.R.id.content);
        if (view != null) {
            view.setOnClickListener(v -> Log.d("TAG", "Hello, lambda! My view is: " + v));
        }

        DummyComponent component = DaggerDummyComponent.builder().build();
        component.inject(this);

        mTextView.setText(
                String.format("%s %s, --from %s.", getString(R.string.dummy_library_android_str),
                        mDummyAndroidClass.getAndroidWord(this), mDummyJavaClass.getJavaWord()));

        if (BuildConfig.CAN_JUMP) {
            mTextView.setOnClickListener(
                    v -> startActivity(new Intent(MainActivity.this, DummyActivity.class)));
        }

        Log.d("test", "1 + 2 = " + new Calc(new CalcMonitor(this)).add(1, 2));

        RxScreenshotDetector.start(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String path) {
                        mTextView.setText(mTextView.getText() + "\nScreenshot: " + path);
                    }

                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }
                });
        GithubUser user = GithubUser.create(100, "OkBuck");
        Toast.makeText(this, user.login(), Toast.LENGTH_SHORT).show();
        GithubRepo repo = GithubRepo.create(100, "OkBuck", "auto buck");
        Toast.makeText(this, repo.name() + ": " + repo.description(), Toast.LENGTH_SHORT).show();

        KotlinDataClass data = new KotlinDataClass("foo", R.string.foo);
        Pojo pojo = new Pojo();
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}

class Transaction<D> {

    public static final class DynamicBuilder<D> {
        private ViewProviderFactory<D> viewProviderFactory;

        DynamicBuilder(final ViewProviderFactory<D> provider) {
             viewProviderFactory = provider;
        }

        public Transaction build(D dependency) {
            viewProviderFactory.create(dependency);
            return new Transaction();
        }
    }
}

abstract class RibletViewProvider extends ViewProvider {

  public final Router hostRouter;

  public RibletViewProvider(Router hostRouter) {
    this.hostRouter = hostRouter;
  }

  public abstract void buildViewRouter();

  public interface Factory extends ViewProviderFactory<Router> {

    @Override
    RibletViewProvider create(Router hostRouter);
  }
}

class Router {
    int a;
    Router(int a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "Router " + this.a;
    }
}

abstract class ViewProvider {

}

interface ViewProviderFactory<D> {
  ViewProvider create(D dependency);
}

