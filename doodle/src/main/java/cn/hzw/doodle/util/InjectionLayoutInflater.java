package cn.hzw.doodle.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater.Factory;
import android.view.View.OnClickListener;


import java.util.WeakHashMap;

import org.xmlpull.v1.XmlPullParser;


public class InjectionLayoutInflater extends LayoutInflater implements Factory {
    private static final String[] sClassPrefixList = new String[]{"android.widget.", "android.webkit.", "android.app."};
    private static final WeakHashMap<Context, InjectionLayoutInflater> WEAK_HASH_MAP = new WeakHashMap();
    private InjectionLayoutInflater.OnViewCreatedListener mOnViewCreatedListener;

    protected InjectionLayoutInflater(Context context) {
        super(context);
        this.setFactory(this);
    }

    protected InjectionLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        this.setFactory(this);
    }

    public static InjectionLayoutInflater from(Context context) {
        InjectionLayoutInflater layoutInflater = (InjectionLayoutInflater) WEAK_HASH_MAP.get(context);
        if (layoutInflater == null) {
            layoutInflater = new InjectionLayoutInflater(context);
            WEAK_HASH_MAP.put(context, layoutInflater);
        }

        return layoutInflater;
    }

    public static InjectionLayoutInflater from(Context context, LayoutInflater original) {
        InjectionLayoutInflater layoutInflater = (InjectionLayoutInflater) WEAK_HASH_MAP.get(context);
        if (layoutInflater == null) {
            layoutInflater = new InjectionLayoutInflater(original, context);
            WEAK_HASH_MAP.put(context, layoutInflater);
        }

        return layoutInflater;
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new InjectionLayoutInflater(this, newContext);
    }

    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot, InjectionLayoutInflater.OnViewCreatedListener listener) {
        this.mOnViewCreatedListener = listener;
        View view = super.inflate(parser, root, attachToRoot);
        this.mOnViewCreatedListener = null;
        return view;
    }

    public View inflate(XmlPullParser parser, ViewGroup root, InjectionLayoutInflater.OnViewCreatedListener listener) {
        this.mOnViewCreatedListener = listener;
        View view = super.inflate(parser, root);
        this.mOnViewCreatedListener = null;
        return view;
    }

    public View inflate(int resource, ViewGroup root, InjectionLayoutInflater.OnViewCreatedListener listener) {
        this.mOnViewCreatedListener = listener;
        View view = super.inflate(resource, root);
        this.mOnViewCreatedListener = null;
        return view;
    }

    public View inflate(int resource, ViewGroup root, boolean attachToRoot, InjectionLayoutInflater.OnViewCreatedListener listener) {
        this.mOnViewCreatedListener = listener;
        View view = super.inflate(resource, root, attachToRoot);
        this.mOnViewCreatedListener = null;
        return view;
    }

    protected View onCreateView(View parent, String name, AttributeSet attrs, InjectionLayoutInflater.OnViewCreatedListener listener) throws ClassNotFoundException {
        this.mOnViewCreatedListener = listener;
        View view = this.onCreateView(parent, name, attrs);
        this.mOnViewCreatedListener = null;
        return view;
    }

    protected View onCreateView(String name, AttributeSet attrs, InjectionLayoutInflater.OnViewCreatedListener listener) throws ClassNotFoundException {
        this.mOnViewCreatedListener = listener;
        View view = this.onCreateView(name, attrs);
        this.mOnViewCreatedListener = null;
        return view;
    }

    protected View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        View view = super.onCreateView(parent, name, attrs);
        return this.mOnViewCreatedListener != null ? this.mOnViewCreatedListener.onViewCreated(this.getContext(), parent, view, attrs) : view;
    }

    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        View view = null;
        if (-1 == name.indexOf(46)) {
            String[] var4 = sClassPrefixList;
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String prefix = var4[var6];

                try {
                    view = this.createView(name, prefix, attrs);
                    if (view != null) {
                        break;
                    }
                } catch (ClassNotFoundException var9) {
                }
            }
        } else {
            view = this.createView(name, (String) null, attrs);
        }

        if (view == null) {
            view = super.onCreateView(name, attrs);
        }

        return this.mOnViewCreatedListener != null ? this.mOnViewCreatedListener.onViewCreated(this.getContext(), (View) null, view, attrs) : view;
    }

    public void setFactory2(Factory2 factory) {
        super.setFactory2(factory);
    }

    public void setFactory(Factory factory) {
        super.setFactory(factory);
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        try {
            return this.onCreateView(name, attrs);
        } catch (ClassNotFoundException var6) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(var6);
            throw ie;
        }
    }

    public static InjectionLayoutInflater.OnViewCreatedListener getViewOnClickListenerInjector(final OnClickListener clickListener) {
        return clickListener == null ? null : new InjectionLayoutInflater.OnViewCreatedListener() {
            public View onViewCreated(Context context, View parent, View view, AttributeSet attrs) {
//                TypedArray a = context.obtainStyledAttributes(attrs, styleable.View);
//                if (a.getBoolean(styleable.View_injectListener, false)) {
//                    view.setOnClickListener(clickListener);
//                }
//                a.recycle();
                return view;
            }
        };
    }

    public static InjectionLayoutInflater.OnViewCreatedListener getViewInjector(final Object object) {
        return object == null ? null : new InjectionLayoutInflater.OnViewCreatedListener() {
            public View onViewCreated(Context context, View parent, View view, AttributeSet attrs) {
                ViewInjectProcessor.process(object, view);
                return view;
            }
        };
    }

    public static InjectionLayoutInflater.OnViewCreatedListener merge(final InjectionLayoutInflater.OnViewCreatedListener... listeners) {
        return listeners == null ? null : new InjectionLayoutInflater.OnViewCreatedListener() {
            public View onViewCreated(Context context, View parent, View view, AttributeSet attrs) {
                InjectionLayoutInflater.OnViewCreatedListener[] var5 = listeners;
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    InjectionLayoutInflater.OnViewCreatedListener listener = var5[var7];
                    view = listener.onViewCreated(context, parent, view, attrs);
                }

                return view;
            }
        };
    }

    public interface OnViewCreatedListener {
        View onViewCreated(Context var1, View var2, View var3, AttributeSet var4);
    }
}
