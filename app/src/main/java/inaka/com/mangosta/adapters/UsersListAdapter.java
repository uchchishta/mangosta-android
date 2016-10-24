package inaka.com.mangosta.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jxmpp.jid.EntityFullJid;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import inaka.com.mangosta.R;
import inaka.com.mangosta.models.User;
import inaka.com.mangosta.utils.NavigateToUserProfile;
import inaka.com.mangosta.utils.UserEvent;
import inaka.com.mangosta.xmpp.XMPPSession;
import inaka.com.mangosta.xmpp.XMPPUtils;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    private List<User> mUsers;
    private Context mContext;
    private boolean mIsLoading;
    private boolean mShowAdd;
    private boolean mShowRemove;

    public UsersListAdapter(Context context, List<User> users, boolean showAdd, boolean showRemove) {
        this.mContext = context;
        this.mUsers = users;
        this.mShowAdd = showAdd;
        this.mShowRemove = showRemove;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.mIsLoading = isLoading;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }
    }

    public static class UserViewHolder extends UsersListAdapter.ViewHolder {
        @Bind(R.id.imageUserAvatar)
        ImageView imageUserAvatar;

        @Bind(R.id.textUserLogin)
        TextView textUserLogin;

        @Bind(R.id.textUserName)
        TextView textUserName;

        @Bind(R.id.addUserToChatButton)
        Button addUserToChatButton;

        @Bind(R.id.removeUserFromChatButton)
        Button removeUserFromChatButton;

        private Context mContext;

        public UserViewHolder(View view, Context context) {
            super(view);
            this.mContext = context;
            ButterKnife.bind(this, view);
        }

        public void bind(final User user, boolean showAdd, boolean showRemove) {
            Picasso.with(mContext).load(R.mipmap.ic_user).noFade().fit().into(imageUserAvatar);
            textUserLogin.setText(user.getLogin());

            if (user.getName() != null) {
                textUserName.setText(user.getName());
            } else {
                textUserName.setText("");
            }

            if (isAuthorizedXMPPUser(user)) {
                addUserToChatButton.setVisibility(View.INVISIBLE);
                removeUserFromChatButton.setVisibility(View.INVISIBLE);
            } else {
                if (showAdd) {
                    addUserToChatButton.setVisibility(View.VISIBLE);
                } else {
                    addUserToChatButton.setVisibility(View.INVISIBLE);
                }

                if (showRemove) {
                    removeUserFromChatButton.setVisibility(View.VISIBLE);
                } else {
                    removeUserFromChatButton.setVisibility(View.INVISIBLE);
                }
            }

            addUserToChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new UserEvent(UserEvent.Type.ADD_USER, user));
                }
            });

            removeUserFromChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new UserEvent(UserEvent.Type.REMOVE_USER, user));
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavigateToUserProfile.go(user, mContext);
                }
            });

        }

    }

    private static boolean isAuthorizedXMPPUser(User user) {
        EntityFullJid userJid = XMPPSession.getInstance().getXMPPConnection().getUser();
        return userJid != null && user.getLogin().equals(XMPPUtils.fromJIDToUserName(userJid.toString()));
    }

    public static class ProgressViewHolder extends UsersListAdapter.ViewHolder {

        @Bind(R.id.progressLoadingItem)
        ProgressBar progressBar;

        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewItemUser = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_user, parent, false);
        return new UserViewHolder(viewItemUser, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder instanceof UserViewHolder) {
            User user = mUsers.get(position);
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            userViewHolder.bind(user, mShowAdd, mShowRemove);
        } else {
            ProgressViewHolder progressViewHolder = (ProgressViewHolder) holder;
            progressViewHolder.progressBar.setIndeterminate(true);

            if (isLoading()) {
                progressViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                progressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
