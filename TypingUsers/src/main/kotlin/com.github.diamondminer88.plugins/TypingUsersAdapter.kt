package com.github.diamondminer88.plugins

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.stores.StoreChannelMembers
import com.discord.stores.StoreStream
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.views.StatusView
import com.discord.views.UsernameView
import com.discord.widgets.channels.memberlist.`GuildMemberListItemGeneratorKt$generateGuildMemberListItems$listItems$1`
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.facebook.drawee.view.SimpleDraweeView
import rx.Observable

@SuppressLint("NotifyDataSetChanged")
class TypingUsersAdapter(
	userIdsObservable: Observable<Set<Long>>,
	private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<ChannelMembersListViewHolderMember>() {
	private var users: LongArray = longArrayOf()

	init {
		userIdsObservable.subscribe {
			users = this.toLongArray()
			notifyDataSetChanged()
		}
	}

	override fun getItemCount(): Int = users.size
	override fun getItemId(position: Int): Long = users[position]

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelMembersListViewHolderMember {
		val inflater = LayoutInflater.from(parent.context)
		val rootView = inflater.inflate(Utils.getResId("widget_channel_members_list_item_user", "layout"), parent, false)

		val binding = WidgetChannelMembersListItemUserBinding(
			rootView as ConstraintLayout,
			rootView.findViewById(Utils.getResId("channel_members_list_item_avatar", "id")) as SimpleDraweeView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_boosted_indicator", "id")) as ImageView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_game", "id")) as SimpleDraweeSpanTextView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_group_owner_indicator", "id")) as ImageView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_name", "id")) as UsernameView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_presence", "id")) as StatusView,
			rootView.findViewById(Utils.getResId("channel_members_list_item_rich_presence_iv", "id")) as ImageView,
		)

		return ChannelMembersListViewHolderMember(binding)
	}

	override fun onBindViewHolder(holder: ChannelMembersListViewHolderMember, position: Int) {
		val userId = users[position]

		val memberRow = StoreChannelMembers.`access$makeRowMember`( // @formatter:off
			StoreStream.getChannelMembers(),
			/* guildId = */ StoreStream.getGuildSelected().selectedGuildId,
			/* userId = */ userId,
			/* allowOwnerIndicator = */ true,
		) // @formatter:on

		val storeStream = StoreStream.Companion.`access$getCollector$p`(StoreStream.Companion)
		val guild = StoreStream.getGuilds().getGuild(StoreStream.getGuildSelected().selectedGuildId)
		val memberItemGenerator = `GuildMemberListItemGeneratorKt$generateGuildMemberListItems$listItems$1`(
			StoreStream.getGuilds(),
			StoreStream.`access$getCustomEmojis$p`(storeStream),
			guild,
			StoreStream.getChannels().getChannel(StoreStream.getChannelsSelected().id),
			guild?.roles?.associateBy { it.id },
		)
		val memberItem = memberItemGenerator.invoke(memberRow) as ChannelMembersListAdapter.Item.Member

		holder.bind(memberItem) {
			WidgetUserSheet.show( // @formatter:off
				/* userId = */ memberItem.userId,
				/* channelId = */ StoreStream.getChannelsSelected().id,
				/* fragmentManager = */ fragmentManager,
				/* guildId = */ memberItem.guildId,
			) // @formatter:on
		}
	}
}
