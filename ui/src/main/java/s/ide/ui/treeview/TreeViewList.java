/*
 * Simple IDE
 * Repository: https://github.com/vxhjsd/Simple-IDE
 * Developer: vxhjsd <vxhjsd@gmail.com>
 *
 * Copyright (C) 2025  vxhjsd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package s.ide.ui.treeview;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import s.ide.ui.R;

public class TreeViewList {

    public static boolean isPath = false;


    public interface LayoutItemType {
        int getLayoutId();
    }

    public static class TreeNode<T extends TreeViewList.LayoutItemType> implements Cloneable {
        private static final int UNDEFINE = -1;
        private T content;
        private TreeViewList.TreeNode parent;
        private List<TreeNode> childList;
        private boolean isExpand;
        private boolean isLocked;
        private int height = UNDEFINE;

        public TreeNode(@NonNull T content) {
            this.content = content;
            this.childList = new ArrayList<>();
        }

        public int getHeight() {
            if (isRoot())
                height = 0;
            else if (height == UNDEFINE)
                height = parent.getHeight() + 1;
            return height;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isLeaf() {
            return childList == null || childList.isEmpty();
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

        public List<TreeViewList.TreeNode> getChildList() {
            return childList;
        }

        public void setChildList(List<TreeViewList.TreeNode> childList) {
            this.childList.clear();
            for (TreeViewList.TreeNode treeNode : childList) {
                addChild(treeNode);
            }
        }

        public void addChild(TreeViewList.TreeNode node) {
            if (childList == null)
                childList = new ArrayList<>();
            childList.add(node);
            node.parent = this;
        }

        public void toggle() {
            isExpand = !isExpand;
        }

        public void collapse() {
            if (isExpand) {
                isExpand = false;
            }
        }

        public void collapseAll() {
            if (childList == null || childList.isEmpty()) {
                return;
            }
            for (TreeViewList.TreeNode child : this.childList) {
                child.collapseAll();
            }
        }

        public void expand() {
            if (!isExpand) {
                isExpand = true;
            }
        }

        public void expandAll() {
            expand();
            if (childList == null || childList.isEmpty()) {
                return;
            }
            for (TreeViewList.TreeNode child : this.childList) {
                child.expandAll();
            }
        }

        public boolean isExpand() {
            return isExpand;
        }

        public TreeViewList.TreeNode getParent() {
            return parent;
        }

        public void setParent(TreeViewList.TreeNode parent) {
            this.parent = parent;
        }

        public TreeViewList.TreeNode<T> lock() {
            isLocked = true;
            return this;
        }

        public TreeViewList.TreeNode<T> unlock() {
            isLocked = false;
            return this;
        }

        public boolean isLocked() {
            return isLocked;
        }

        @NonNull
        @Override
        public String toString() {
            return "TreeNode{" +
                    "content=" + this.content +
                    ", parent=" + (parent == null ? "null" : parent.getContent().toString()) +
                    ", childList=" + (childList == null ? "null" : childList.toString()) +
                    ", isExpand=" + isExpand +
                    '}';
        }

        @NonNull
        @Override
        protected TreeViewList.TreeNode<T> clone() throws CloneNotSupportedException {
            //noinspection unchecked
            TreeViewList.TreeNode<T> tTreeNode = (TreeViewList.TreeNode<T>) super.clone();
            TreeViewList.TreeNode<T> clone = new TreeViewList.TreeNode<>(this.content);
            clone.isExpand = this.isExpand;
            return clone;
        }
    }

    public static class TreeViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final String KEY_IS_EXPAND = "IS_EXPAND";
        private final List<? extends TreeViewList.TreeViewBinder> viewBinders;
        private final List<TreeViewList.TreeNode> displayNodes;
        private int padding = 30;
        private TreeViewList.TreeViewAdapter.OnTreeNodeListener onTreeNodeListener;
        private boolean toCollapseChild;

        public TreeViewAdapter(List<? extends TreeViewList.TreeViewBinder> viewBinders) {
            this(null, viewBinders);
        }

        public TreeViewAdapter(List<TreeViewList.TreeNode> nodes, List<? extends TreeViewList.TreeViewBinder> viewBinders) {
            displayNodes = new ArrayList<>();
            if (nodes != null)
                findDisplayNodes(nodes);
            this.viewBinders = viewBinders;
        }

        private void findDisplayNodes(List<TreeViewList.TreeNode> nodes) {
            for (TreeViewList.TreeNode node : nodes) {
                displayNodes.add(node);
                if (!node.isLeaf() && node.isExpand())
                    //noinspection unchecked
                    findDisplayNodes(node.getChildList());
            }
        }

        @Override
        public int getItemViewType(int position) {
            return displayNodes.get(position).getContent().getLayoutId();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(viewType, parent, false);
            if (viewBinders.size() == 1)
                return viewBinders.get(0).provideViewHolder(v);
            for (TreeViewList.TreeViewBinder viewBinder : viewBinders) {
                if (viewBinder.getLayoutId() == viewType)
                    return viewBinder.provideViewHolder(v);
            }
            return viewBinders.get(0).provideViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                Bundle b = (Bundle) payloads.get(0);
                for (String key : b.keySet()) {
                    if (key.equals(KEY_IS_EXPAND)) {
                        if (onTreeNodeListener != null)
                            onTreeNodeListener.onToggle(b.getBoolean(key), holder);
                    }
                }
            }
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            holder.itemView.setPaddingRelative(displayNodes.get(position).getHeight() * padding, 3, 3, 3);

            final TextView txt = holder.itemView.findViewById(R.id.tv_name);

            final String[] clickedPath = {""};

            holder.itemView.setOnClickListener(v -> {
                TreeViewList.TreeNode selectedNode = displayNodes.get(holder.getLayoutPosition());
                try {
                    long lastClickTime = (long) holder.itemView.getTag();
                    if (System.currentTimeMillis() - lastClickTime < 500)
                        return;
                } catch (Exception e) {
                    holder.itemView.setTag(System.currentTimeMillis());
                }
                holder.itemView.setTag(System.currentTimeMillis());


                try {
                    TreeViewList.Dir dirNode = (TreeViewList.Dir) selectedNode.getContent();
                    clickedPath[0] = dirNode.dirName;
                } catch (Exception e) {
                    TreeViewList.File fileNode = (TreeViewList.File) selectedNode.getContent();
                    clickedPath[0] = fileNode.fileName;
                }

                if (onTreeNodeListener != null && onTreeNodeListener.onClick(clickedPath[0],
                        selectedNode, holder))
                    return;
                if (selectedNode.isLeaf())
                    return;
                if (selectedNode.isLocked()) return;
                boolean isExpand = selectedNode.isExpand();
                int positionStart = displayNodes.indexOf(selectedNode) + 1;
                if (!isExpand) {
                    notifyItemRangeInserted(positionStart, addChildNodes(selectedNode, positionStart));
                } else {
                    notifyItemRangeRemoved(positionStart, removeChildNodes(selectedNode, true));
                }
            });


            holder.itemView.setOnLongClickListener(v -> {
                TreeViewList.TreeNode selectedNode = displayNodes.get(holder.getLayoutPosition());

                try {
                    TreeViewList.Dir dirNode = (TreeViewList.Dir) selectedNode.getContent();
                    clickedPath[0] = dirNode.dirName;
                } catch (Exception e) {
                    TreeViewList.File fileNode = (TreeViewList.File) selectedNode.getContent();
                    clickedPath[0] = fileNode.fileName;
                }

                onTreeNodeListener.onLongClick(clickedPath[0]);


                return true;
            });


            for (TreeViewList.TreeViewBinder viewBinder : viewBinders) {
                if (viewBinder.getLayoutId() == displayNodes.get(position).getContent().getLayoutId())
                    //noinspection unchecked
                    viewBinder.bindView(holder, position, displayNodes.get(position));
            }
        }

        private int addChildNodes(TreeViewList.TreeNode pNode, int startIndex) {
            //noinspection unchecked
            List<TreeViewList.TreeNode> childList = pNode.getChildList();
            int addChildCount = 0;
            for (TreeViewList.TreeNode treeNode : childList) {
                displayNodes.add(startIndex + addChildCount++, treeNode);
                if (treeNode.isExpand()) {
                    addChildCount += addChildNodes(treeNode, startIndex + addChildCount);
                }
            }
            if (!pNode.isExpand())
                pNode.toggle();
            return addChildCount;
        }

        private void removeChildNodes(TreeViewList.TreeNode pNode) {
            removeChildNodes(pNode, true);
        }

        private int removeChildNodes(TreeViewList.TreeNode pNode, boolean shouldToggle) {
            if (pNode.isLeaf())
                return 0;
            List<TreeViewList.TreeNode> childList = pNode.getChildList();
            int removeChildCount = childList.size();
            displayNodes.removeAll(childList);
            for (TreeViewList.TreeNode child : childList) {
                if (child.isExpand()) {
                    if (toCollapseChild)
                        child.toggle();
                    removeChildCount += removeChildNodes(child, false);
                }
            }
            if (shouldToggle)
                pNode.toggle();
            return removeChildCount;
        }

        @Override
        public int getItemCount() {
            return displayNodes == null ? 0 : displayNodes.size();
        }

        public void setPadding(int padding) {
            this.padding = padding;
        }

        public void ifCollapseChildWhileCollapseParent(boolean toCollapseChild) {
            this.toCollapseChild = toCollapseChild;
        }

        public void setOnTreeNodeListener(TreeViewList.TreeViewAdapter.OnTreeNodeListener onTreeNodeListener) {
            this.onTreeNodeListener = onTreeNodeListener;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void refresh(List<TreeViewList.TreeNode> treeNodes) {
            displayNodes.clear();
            findDisplayNodes(treeNodes);
            notifyDataSetChanged();
        }

        public Iterator<TreeNode> getDisplayNodesIterator() {
            return displayNodes.iterator();
        }

        private void notifyDiff(final List<TreeViewList.TreeNode> temp) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return temp.size();
                }

                @Override
                public int getNewListSize() {
                    return displayNodes.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return TreeViewList.TreeViewAdapter.this.areItemsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return TreeViewList.TreeViewAdapter.this.areContentsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
                }

                @Nullable
                @Override
                public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                    return TreeViewList.TreeViewAdapter.this.getChangePayload(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
                }
            });
            diffResult.dispatchUpdatesTo(this);
        }

        private Object getChangePayload(TreeViewList.TreeNode oldNode, TreeViewList.TreeNode newNode) {
            Bundle diffBundle = new Bundle();
            if (newNode.isExpand() != oldNode.isExpand()) {
                diffBundle.putBoolean(KEY_IS_EXPAND, newNode.isExpand());
            }
            if (diffBundle.isEmpty())
                return null;
            return diffBundle;
        }

        private boolean areContentsTheSame(TreeViewList.TreeNode oldNode, TreeViewList.TreeNode newNode) {
            return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent())
                    && oldNode.isExpand() == newNode.isExpand();
        }

        private boolean areItemsTheSame(TreeViewList.TreeNode oldNode, TreeViewList.TreeNode newNode) {
            return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent());
        }

        public void collapseAll() {
            List<TreeViewList.TreeNode> temp = backupDisplayNodes();
            List<TreeViewList.TreeNode> roots = new ArrayList<>();
            for (TreeViewList.TreeNode displayNode : displayNodes) {
                if (displayNode.isRoot())
                    roots.add(displayNode);
            }
            for (TreeViewList.TreeNode root : roots) {
                if (root.isExpand())
                    removeChildNodes(root);
            }
            notifyDiff(temp);
        }

        @NonNull
        private List<TreeViewList.TreeNode> backupDisplayNodes() {
            List<TreeViewList.TreeNode> temp = new ArrayList<>();
            for (TreeViewList.TreeNode displayNode : displayNodes) {
                try {
                    temp.add(displayNode.clone());
                } catch (CloneNotSupportedException e) {
                    temp.add(displayNode);
                }
            }
            return temp;
        }

        public void collapseNode(TreeViewList.TreeNode pNode) {
            List<TreeViewList.TreeNode> temp = backupDisplayNodes();
            removeChildNodes(pNode);
            notifyDiff(temp);
        }

        public void collapseBrotherNode(TreeViewList.TreeNode pNode) {
            List<TreeViewList.TreeNode> temp = backupDisplayNodes();
            if (pNode.isRoot()) {
                List<TreeViewList.TreeNode> roots = new ArrayList<>();
                for (TreeViewList.TreeNode displayNode : displayNodes) {
                    if (displayNode.isRoot())
                        roots.add(displayNode);
                }
                //Close all root nodes.
                for (TreeViewList.TreeNode root : roots) {
                    if (root.isExpand() && !root.equals(pNode))
                        removeChildNodes(root);
                }
            } else {
                TreeViewList.TreeNode parent = pNode.getParent();
                if (parent == null)
                    return;
                //noinspection unchecked
                List<TreeViewList.TreeNode> childList = parent.getChildList();
                for (TreeViewList.TreeNode node : childList) {
                    if (node.equals(pNode) || !node.isExpand())
                        continue;
                    removeChildNodes(node);
                }
            }
            notifyDiff(temp);
        }

        public interface OnTreeNodeListener {

            boolean onClick(String clickedPath, TreeViewList.TreeNode node, RecyclerView.ViewHolder holder);

            void onToggle(boolean isExpand, RecyclerView.ViewHolder holder);

            void onLongClick(String clickedPath);
        }

    }

    public static abstract class TreeViewBinder<VH extends RecyclerView.ViewHolder> implements TreeViewList.LayoutItemType {
        public abstract VH provideViewHolder(View itemView);

        public abstract void bindView(VH holder, int position, TreeViewList.TreeNode node);

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View rootView) {
                super(rootView);
            }

            protected <T extends View> T findViewById(@IdRes int id) {
                return itemView.findViewById(id);
            }
        }

    }


    public static class FileNodeBinder extends TreeViewList.TreeViewBinder<TreeViewList.FileNodeBinder.ViewHolder> {
        @Override
        public TreeViewList.FileNodeBinder.ViewHolder provideViewHolder(View itemView) {
            return new TreeViewList.FileNodeBinder.ViewHolder(itemView);
        }

        @Override
        public void bindView(TreeViewList.FileNodeBinder.ViewHolder holder, int position, TreeViewList.TreeNode node) {
            TreeViewList.File fileNode = (TreeViewList.File) node.getContent();
            if (TreeViewList.isPath) {
                holder.tvName.setText(Uri.parse(fileNode.fileName).getLastPathSegment());
            } else {
                holder.tvName.setText(fileNode.fileName);
            }
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_file;
        }

        public static class ViewHolder extends TreeViewList.TreeViewBinder.ViewHolder {
            public TextView tvName;

            public ViewHolder(View rootView) {
                super(rootView);
                this.tvName = rootView.findViewById(R.id.tv_name);
            }

        }
    }


    public static class DirectoryNodeBinder extends TreeViewList.TreeViewBinder<TreeViewList.DirectoryNodeBinder.ViewHolder> {
        @Override
        public TreeViewList.DirectoryNodeBinder.ViewHolder provideViewHolder(View itemView) {
            return new TreeViewList.DirectoryNodeBinder.ViewHolder(itemView);
        }

        @Override
        public void bindView(TreeViewList.DirectoryNodeBinder.ViewHolder holder, int position, TreeViewList.TreeNode node) {
            holder.ivArrow.setRotation(0);
            holder.ivArrow.setImageResource(R.drawable.ic_expand);
            int rotateDegree = node.isExpand() ? 90 : 0;
            holder.ivArrow.setRotation(rotateDegree);
            TreeViewList.Dir dirNode = (TreeViewList.Dir) node.getContent();

            if (TreeViewList.isPath) {
                holder.tvName.setText(Uri.parse(dirNode.dirName).getLastPathSegment());
            } else {
                holder.tvName.setText(dirNode.dirName);
            }

            if (node.isLeaf())
                holder.ivArrow.setVisibility(View.INVISIBLE);
            else holder.ivArrow.setVisibility(View.VISIBLE);
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_dir;
        }

        public static class ViewHolder extends TreeViewList.TreeViewBinder.ViewHolder {
            private final ImageView ivArrow;
            private final TextView tvName;

            public ViewHolder(View rootView) {
                super(rootView);
                this.ivArrow = rootView.findViewById(R.id.iv_arrow);
                this.tvName = rootView.findViewById(R.id.tv_name);
            }

            public ImageView getIvArrow() {
                return ivArrow;
            }

            public TextView getTvName() {
                return tvName;
            }
        }
    }


    public static class Dir implements TreeViewList.LayoutItemType {
        public String dirName;

        public Dir(String dirName) {
            this.dirName = dirName;
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_dir;
        }
    }


    public static class File implements TreeViewList.LayoutItemType {
        public String fileName;

        public File(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_file;
        }
    }

}
