<template>
  <div class="community-page">
    <!-- 页头 -->
    <div class="community-header">
      <div>
        <div class="header-title">🐾 宠物社区</div>
        <div class="header-sub">分享养宠经验，结识宠友</div>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧主内容 -->
      <el-col :span="16">
        <!-- 发布框 -->
        <div class="publish-card">
          <el-input v-model="postContent" type="textarea" :rows="4"
            placeholder="分享你的养宠经验，或用 #话题# 参与讨论..."
            maxlength="500" show-word-limit />
          <div v-if="uploadedImages.length > 0" class="image-preview">
            <div class="image-item" v-for="(img, idx) in uploadedImages" :key="idx">
              <el-image :src="img" fit="cover" />
              <span class="remove-img" @click="uploadedImages.splice(idx, 1)">×</span>
            </div>
          </div>
          <div class="publish-footer">
            <div class="publish-tools">
              <el-upload :action="uploadUrl + '/upload/images'" :headers="uploadHeaders"
                :show-file-list="false" :before-upload="beforeImageUpload"
                :on-success="handleImageSuccess" multiple accept="image/*">
                <button class="tool-btn">📷 图片</button>
              </el-upload>
              <button class="tool-btn" @click="ElMessage.info('位置功能开发中')">📍 位置</button>
            </div>
            <button class="publish-btn" @click="handlePublish"
              :disabled="!postContent.trim() && uploadedImages.length === 0">
              {{ publishing ? '发布中...' : '发布' }}
            </button>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="post-card" v-for="post in posts" :key="post.id">
          <div class="post-header">
            <div class="post-user">
              <el-avatar :size="42" :src="post.avatar" class="user-avatar">
                {{ (post.username || '?').charAt(0).toUpperCase() }}
              </el-avatar>
              <div>
                <div class="post-username">{{ post.username }}</div>
                <div class="post-time">{{ post.time }}</div>
              </div>
            </div>
            <el-dropdown>
              <span class="more-btn">···</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="ElMessage.info('举报功能开发中')">举报</el-dropdown-item>
                  <el-dropdown-item v-if="post.isMine" @click="handleDeletePost(post)" style="color:#dc2626">删除</el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" @click="handleAdminDelete(post)" style="color:#dc2626">管理员删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="post-content">
            <p v-if="post.content" v-html="formatContent(post.content)"></p>
            <p v-else class="empty-content">（该帖子暂无内容）</p>
            <div class="post-images" v-if="post.images && post.images.length > 0">
              <el-image v-for="(img, i) in post.images" :key="i" :src="img"
                fit="cover" :preview-src-list="post.images" class="post-img" />
            </div>
          </div>

          <div class="post-actions">
            <button class="action-btn" :class="{ liked: post.isLiked }" @click="handleLike(post)">
              {{ post.isLiked ? '❤️' : '🤍' }} {{ post.likes }}
            </button>
            <button class="action-btn" @click="handleComment(post)">
              💬 {{ post.comments }}
            </button>
            <button class="action-btn" @click="handleShare(post)">
              🔗 分享
            </button>
          </div>
        </div>

        <el-empty v-if="!loading && posts.length === 0" description="暂无动态，快来发布第一条吧～" />
        <div class="load-more" v-if="hasMore" @click="loadMore">加载更多</div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :span="8">
        <!-- 热门话题 -->
        <div class="sidebar-card">
          <div class="sidebar-title">🔥 热门话题</div>
          <div class="topic-list">
            <div class="topic-item" v-for="(topic, idx) in hotTopics" :key="topic.id"
              @click="filterByTopic(topic)">
              <div class="topic-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</div>
              <div class="topic-info">
                <div class="topic-name">#{{ topic.name }}</div>
                <div class="topic-count">{{ topic.count.toLocaleString() }} 讨论</div>
              </div>
              <span class="topic-arrow">›</span>
            </div>
          </div>
        </div>

        <!-- 推荐关注 -->
        <div class="sidebar-card">
          <div class="sidebar-title">👥 推荐关注</div>
          <div class="user-list">
            <div class="user-item" v-for="user in recommendUsers" :key="user.id">
              <el-avatar :size="44" :src="user.avatar" class="user-avatar">
                {{ user.username.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-desc">{{ user.desc }}</div>
              </div>
              <button class="follow-btn" :class="{ followed: user.followed }"
                @click="toggleFollow(user)">
                {{ user.followed ? '已关注' : '关注' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 达人榜 -->
        <div class="sidebar-card">
          <div class="sidebar-title">🏆 本周达人榜</div>
          <div class="rank-list">
            <div class="rank-item" v-for="(u, i) in topUsers" :key="i">
              <span class="rank-medal">{{ ['🥇','🥈','🥉'][i] || (i+1) }}</span>
              <el-avatar :size="32" class="user-avatar">{{ u.name.charAt(0) }}</el-avatar>
              <span class="rank-name">{{ u.name }}</span>
              <span class="rank-score">{{ u.score }} 赞</span>
            </div>
          </div>
        </div>

        <!-- 管理员工具 -->
        <div class="sidebar-card admin-card" v-if="isAdmin">
          <div class="sidebar-title">⚙️ 管理工具</div>
          <div class="admin-links">
            <button class="admin-link" @click="$router.push('/dashboard/user-management')">👥 用户管理</button>
            <button class="admin-link" @click="$router.push('/dashboard/service-management')">🔧 服务管理</button>
            <button class="admin-link" @click="showAuditPanel = true">📋 内容审核</button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 评论对话框 -->
    <el-dialog v-model="commentDialogVisible" title="评论" width="580px">
      <div class="comment-post-preview" v-if="currentPost">
        <el-avatar :size="32" class="user-avatar">{{ (currentPost.username || '?').charAt(0) }}</el-avatar>
        <div>
          <span class="fw600">{{ currentPost.username }}</span>
          <span class="ml8 gray">{{ currentPost.time }}</span>
          <p class="mt4 gray">{{ currentPost.content }}</p>
        </div>
      </div>
      <el-input v-model="commentContent" type="textarea" :rows="3"
        placeholder="写下你的评论..." maxlength="200" show-word-limit />
      <div class="comment-submit">
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button class="btn-brand" @click="submitComment" :disabled="!commentContent.trim()">发表评论</el-button>
      </div>
      <div class="comment-list-wrap">
        <div class="comment-list-title">全部评论 ({{ currentPost?.comments || 0 }})</div>
        <div v-if="comments.length === 0 && !commentLoading" class="no-comment">暂无评论，快来抢沙发吧 🛋️</div>
        <div class="comment-item" v-for="c in comments" :key="c.id">
          <el-avatar :size="32" class="user-avatar">{{ (c.user?.nickname || c.user?.username || 'U').charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="fw600">{{ c.user?.nickname || c.user?.username || '匿名' }}</span>
              <span class="gray ml8">{{ formatTime(c.createTime) }}</span>
            </div>
            <p class="comment-text">{{ c.content }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { communityAPI } from '@/api/community'
import { useUserStore } from '@/store/user'
import { follow as followApi, unfollow as unfollowApi, checkFollow } from '@/api/follow'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const postContent = ref('')
const posts = ref([])
const loading = ref(false)
const publishing = ref(false)
const uploadedImages = ref([])
const hasMore = ref(false)
const currentPage = ref(1)
const showAuditPanel = ref(false)

const uploadUrl = '/api'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

// 热门话题
const hotTopics = ref([
  { id: 1, name: '狗狗训练技巧', count: 1234 },
  { id: 2, name: '猫咪日常', count: 987 },
  { id: 3, name: '宠物健康', count: 756 },
  { id: 4, name: '萌宠分享', count: 654 },
  { id: 5, name: '养宠经验', count: 543 }
])

// 推荐用户
const recommendUsers = ref([
  { id: 1, username: '爱宠达人', desc: '资深养宠博主 · 10年经验', avatar: '', followed: false },
  { id: 2, username: '宠物医生小王', desc: '执业兽医 · 专业健康建议', avatar: '', followed: false },
  { id: 3, username: '萌宠摄影师', desc: '宠物摄影爱好者 · 分享日常', avatar: '', followed: false }
])

// 达人榜
const topUsers = ref([
  { name: '爱宠达人', score: 2341 },
  { name: '宠物医生小王', score: 1876 },
  { name: '萌宠摄影师', score: 1432 },
  { name: 'weqe', score: 987 },
  { name: '张三', score: 654 }
])

// 模拟帖子（API失败时兜底）
const mockPosts = [
  {
    id: 101, username: '系统管理员', avatar: '', time: '2小时前', isMine: false,
    content: '🐾 【养宠干货】新手养狗必看！\n\n1. 疫苗接种：幼犬需在8周龄开始接种，完成三针基础免疫后每年加强一次。\n2. 驱虫计划：每月体外驱虫，每季度体内驱虫，保护宠物和家人健康。\n3. 饮食管理：选择适龄全价粮，避免喂食巧克力、葡萄、洋葱等有毒食物。\n4. 定期体检：建议每年至少一次全面体检，早发现早治疗。\n\n#宠物健康 #养宠经验',
    images: [], likes: 256, comments: 18, isLiked: false
  },
  {
    id: 102, username: '张三', avatar: '', time: '5小时前', isMine: false,
    content: '今天带我家金毛去公园玩，它特别开心！分享一些训练技巧给大家：\n1. 奖励要及时，行为发生后3秒内给予奖励\n2. 每次训练不超过15分钟，保持宠物注意力\n3. 用正向强化，避免惩罚 #狗狗训练技巧',
    images: ['https://images.unsplash.com/photo-1633722715463-d30f4f325e24?w=400'],
    likes: 128, comments: 23, isLiked: false
  },
  {
    id: 103, username: '李四', avatar: '', time: '1天前', isMine: false,
    content: '我家猫咪今天特别乖，给它买了新玩具，玩得不亦乐乎 😊 猫咪日常分享～ #猫咪日常',
    images: ['https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400'],
    likes: 89, comments: 15, isLiked: false
  }
]

// 评论相关
const commentDialogVisible = ref(false)
const currentPost = ref(null)
const commentContent = ref('')
const comments = ref([])
const commentLoading = ref(false)

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await communityAPI.getPostList({ page: currentPage.value, size: 10 })
    if (res.data.code === 200) {
      const records = res.data.data.records || []
      const mapped = records.map(p => ({
        id: p.id,
        username: p.user?.nickname || p.user?.username || '匿名用户',
        avatar: p.user?.avatar || '',
        time: formatTime(p.createTime),
        content: p.content || '',
        images: p.images ? (typeof p.images === 'string' ? JSON.parse(p.images) : p.images) : [],
        likes: p.likesCount || 0,
        comments: p.commentsCount || 0,
        isLiked: p.isLiked || false,
        isMine: p.isMine || false
      }))
      posts.value = currentPage.value === 1 ? mapped : [...posts.value, ...mapped]
      hasMore.value = records.length === 10
    } else {
      if (currentPage.value === 1) posts.value = mockPosts
    }
  } catch (e) {
    if (currentPage.value === 1) posts.value = mockPosts
  } finally { loading.value = false }
}

const loadMore = () => { currentPage.value++; loadPosts() }

const loadHotTopics = async () => {
  try {
    const res = await communityAPI.getHotTopics({ limit: 5 })
    if (res.data.code === 200) {
      hotTopics.value = res.data.data.map(t => ({ id: t.id, name: t.name, count: t.postsCount || 0 }))
    }
  } catch (e) {}
}

const loadRecommendUsers = async () => {
  try {
    const res = await communityAPI.getRecommendUsers({ limit: 3 })
    if (res.data.code === 200) {
      recommendUsers.value = res.data.data.map(u => ({
        id: u.id, username: u.nickname || u.username,
        desc: u.desc || '宠物爱好者', avatar: u.avatar || '', followed: false
      }))
    }
  } catch (e) {}
}

const handlePublish = async () => {
  if (!postContent.value.trim() && uploadedImages.value.length === 0) {
    ElMessage.warning('请输入内容或上传图片'); return
  }
  publishing.value = true
  try {
    const res = await communityAPI.publishPost({
      content: postContent.value,
      topicTags: extractTopics(postContent.value),
      images: JSON.stringify(uploadedImages.value),
      video: '', location: ''
    })
    if (res.data.code === 200) {
      ElMessage.success('发布成功')
      postContent.value = ''; uploadedImages.value = []
      currentPage.value = 1; loadPosts()
    } else { ElMessage.error(res.data.message || '发布失败') }
  } catch (e) { ElMessage.error('发布失败，请重试') }
  finally { publishing.value = false }
}

const extractTopics = (content) => {
  const m = content.match(/#([^#\s]+)/g) || []
  return m.map(t => t.slice(1)).join(',')
}

const handleLike = async (post) => {
  try {
    const res = await communityAPI.likePost(post.id)
    if (res.data.code === 200) {
      const r = res.data.data
      post.isLiked = r.isLiked
      post.likes += r.isLiked ? 1 : -1
    }
  } catch (e) {
    // 乐观更新
    post.isLiked = !post.isLiked
    post.likes += post.isLiked ? 1 : -1
  }
}

const handleComment = async (post) => {
  currentPost.value = post; commentDialogVisible.value = true
  commentLoading.value = true
  try {
    const res = await communityAPI.getCommentList(post.id, { page: 1, size: 20 })
    comments.value = res.data.code === 200 ? (res.data.data.records || []) : []
  } catch (e) { comments.value = [] }
  finally { commentLoading.value = false }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  try {
    const res = await communityAPI.addComment(currentPost.value.id, { content: commentContent.value })
    if (res.data.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      currentPost.value.comments++
      await handleComment(currentPost.value)
    }
  } catch (e) { ElMessage.error('评论失败') }
}

const handleShare = (post) => {
  navigator.clipboard?.writeText(window.location.href).then(() => ElMessage.success('链接已复制'))
    .catch(() => ElMessage.info('分享功能开发中'))
}

const handleDeletePost = async (post) => {
  try {
    await communityAPI.deletePost(post.id)
    posts.value = posts.value.filter(p => p.id !== post.id)
    ElMessage.success('删除成功')
  } catch (e) { ElMessage.error('删除失败') }
}

const handleAdminDelete = (post) => handleDeletePost(post)

const filterByTopic = (topic) => {
  ElMessage.info(`查看话题 #${topic.name} 的帖子`)
}

const toggleFollow = async (user) => {
  try {
    if (user.followed) {
      await unfollowApi(user.id)
      user.followed = false
      ElMessage.success('已取消关注')
    } else {
      await followApi(user.id)
      user.followed = true
      ElMessage.success(`已关注 ${user.username}`)
    }
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const beforeImageUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片不能超过5MB'); return false }
  if (uploadedImages.value.length >= 9) { ElMessage.error('最多9张图片'); return false }
  return true
}

const handleImageSuccess = (res) => {
  if (res.code === 200) { uploadedImages.value.push(res.data); ElMessage.success('上传成功') }
  else ElMessage.error('上传失败')
}

// 格式化内容，将换行转为 <br>，话题标签高亮
const formatContent = (content) => {
  if (!content) return ''
  return content
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/#([^#\s]+)/g, '<span class="topic-tag">#$1</span>')
    .replace(/\n/g, '<br>')
}

const formatTime = (t) => {
  if (!t) return '刚刚'
  const diff = Date.now() - new Date(t)
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const d = Math.floor(h / 24)
  if (d < 7) return `${d}天前`
  return new Date(t).toLocaleDateString()
}

onMounted(() => { loadPosts(); loadHotTopics(); loadRecommendUsers() })
</script>

<style scoped>
.community-page { padding: 0; }

/* 品牌按钮 */
.btn-brand { background: #5C8AEB !important; border-color: #5C8AEB !important; color: #fff !important; font-weight: 600; }

/* 页头 */
.community-header {
  background: #fff; padding: 20px 28px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-left: 4px solid #5C8AEB;
}
.header-title { font-size: 22px; font-weight: 700; color: #111827; margin-bottom: 4px; }
.header-sub { font-size: 14px; color: #9ca3af; }

/* 发布框 */
.publish-card {
  background: #fff; border-radius: 8px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 16px;
}
.publish-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.publish-tools { display: flex; gap: 8px; }
.tool-btn {
  padding: 6px 14px; border: 1px solid #e5e7eb; border-radius: 20px;
  background: #fff; color: #6b7280; font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.tool-btn:hover { border-color: #5C8AEB; color: #5C8AEB; }
.publish-btn {
  padding: 8px 28px; background: #5C8AEB; color: #fff;
  border: none; border-radius: 20px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
}
.publish-btn:hover:not(:disabled) { background: #4A78D6; }
.publish-btn:disabled { background: #d1d5db; cursor: not-allowed; }

/* 图片预览 */
.image-preview { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
.image-item { position: relative; width: 80px; height: 80px; border-radius: 8px; overflow: hidden; }
.image-item .el-image { width: 100%; height: 100%; }
.remove-img {
  position: absolute; top: 3px; right: 3px; width: 18px; height: 18px;
  background: rgba(0,0,0,0.6); color: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; cursor: pointer; line-height: 1;
}

/* 帖子卡片 */
.post-card {
  background: #fff; border-radius: 8px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 16px;
  transition: all 0.25s;
}
.post-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.post-user { display: flex; align-items: center; gap: 10px; }
.user-avatar { background: #5C8AEB; color: #fff; font-weight: 700; flex-shrink: 0; }
.post-username { font-size: 15px; font-weight: 600; color: #111827; }
.post-time { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.more-btn { font-size: 20px; color: #9ca3af; cursor: pointer; padding: 4px 8px; border-radius: 4px; }
.more-btn:hover { background: #f3f4f6; }

.post-content p { margin: 0 0 12px; line-height: 1.7; color: #374151; white-space: pre-wrap; }
.empty-content { color: #d1d5db; font-style: italic; }
.post-images { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.post-img { width: 120px; height: 120px; border-radius: 8px; object-fit: cover; cursor: pointer; }

.post-actions { display: flex; gap: 16px; padding-top: 14px; border-top: 1px solid #f3f4f6; margin-top: 14px; }
.action-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 6px 14px; border: 1px solid #e5e7eb; border-radius: 20px;
  background: #fff; color: #6b7280; font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.action-btn:hover { border-color: #5C8AEB; color: #5C8AEB; background: rgba(92,138,235,0.08); }
.action-btn.liked { border-color: #dc2626; color: #dc2626; background: #fef2f2; }

/* 话题标签 */
:deep(.topic-tag) { color: #5C8AEB; font-weight: 600; cursor: pointer; }
:deep(.topic-tag:hover) { text-decoration: underline; }

/* 加载更多 */
.load-more {
  text-align: center; padding: 14px; color: #5C8AEB; cursor: pointer;
  background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  font-size: 14px; font-weight: 500;
}
.load-more:hover { background: rgba(92,138,235,0.08); }

/* 侧边栏 */
.sidebar-card {
  background: #fff; border-radius: 8px; padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 16px;
}
.sidebar-title { font-size: 15px; font-weight: 700; color: #111827; margin-bottom: 14px; }

/* 热门话题 */
.topic-list { display: flex; flex-direction: column; gap: 10px; }
.topic-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 10px;
  border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.topic-item:hover { background: rgba(92,138,235,0.08); }
.topic-rank {
  width: 22px; height: 22px; border-radius: 50%; background: #f3f4f6;
  color: #6b7280; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.rank-1 { background: #fef3c7; color: #d97706; }
.rank-2 { background: #f3f4f6; color: #6b7280; }
.rank-3 { background: #fff7ed; color: #ea580c; }
.topic-info { flex: 1; }
.topic-name { font-size: 14px; font-weight: 600; color: #5C8AEB; }
.topic-count { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.topic-arrow { color: #9ca3af; font-size: 16px; }

/* 推荐用户 */
.user-list { display: flex; flex-direction: column; gap: 14px; }
.user-item { display: flex; align-items: center; gap: 10px; }
.user-info { flex: 1; }
.user-name { font-size: 14px; font-weight: 600; color: #111827; }
.user-desc { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.follow-btn {
  padding: 5px 14px; border-radius: 20px; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  border: 1px solid #5C8AEB; background: #fff; color: #5C8AEB;
}
.follow-btn:hover { background: #5C8AEB; color: #fff; }
.follow-btn.followed { background: #f3f4f6; color: #9ca3af; border-color: #e5e7eb; }
.follow-btn.followed:hover { background: #fef2f2; color: #dc2626; border-color: #dc2626; }

/* 达人榜 */
.rank-list { display: flex; flex-direction: column; gap: 10px; }
.rank-item { display: flex; align-items: center; gap: 8px; }
.rank-medal { font-size: 18px; width: 24px; text-align: center; }
.rank-name { flex: 1; font-size: 13px; font-weight: 500; color: #374151; }
.rank-score { font-size: 12px; color: #9ca3af; }

/* 管理工具 */
.admin-card { border: 1px solid #fef3c7; background: #fffbeb; }
.admin-links { display: flex; flex-direction: column; gap: 8px; }
.admin-link {
  padding: 8px 14px; border-radius: 6px; border: 1px solid #e5e7eb;
  background: #fff; color: #374151; font-size: 13px; cursor: pointer;
  text-align: left; transition: all 0.2s;
}
.admin-link:hover { border-color: #5C8AEB; color: #5C8AEB; background: rgba(92,138,235,0.08); }

/* 评论对话框 */
.comment-post-preview {
  display: flex; gap: 10px; padding: 12px; background: #f9fafb;
  border-radius: 8px; margin-bottom: 14px;
}
.comment-submit { display: flex; justify-content: flex-end; gap: 8px; margin-top: 10px; margin-bottom: 20px; }
.comment-list-wrap { max-height: 360px; overflow-y: auto; }
.comment-list-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #f3f4f6; }
.no-comment { text-align: center; color: #9ca3af; padding: 24px 0; font-size: 14px; }
.comment-item { display: flex; gap: 10px; margin-bottom: 14px; }
.comment-body { flex: 1; }
.comment-meta { margin-bottom: 4px; }
.comment-text { font-size: 14px; color: #374151; line-height: 1.6; margin: 0; }

/* 工具类 */
.fw600 { font-weight: 600; }
.ml8 { margin-left: 8px; }
.mt4 { margin-top: 4px; }
.gray { color: #9ca3af; font-size: 13px; }
</style>
