/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  status?: number
  role?: 'USER' | 'MODERATOR' | 'ADMIN'
  mutedUntil?: string
  createdAt?: string
}

/** 统一响应格式 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** 登录响应 */
export interface LoginResult {
  token: string
  user: UserInfo
}

/** 分页数据 */
export interface PageData<T> {
  records: T[]
  total: number
  size: number
  current: number
}

/** 帖子 */
export interface PostInfo {
  id: number
  title: string
  content: string
  userId: number
  username: string
  nickname: string
  avatar?: string
  categoryId: number
  categoryName: string
  tags?: TagInfo[]
  viewCount: number
  likeCount: number
  commentCount: number
  status?: number
  essence?: boolean
  commentEnabled?: boolean
  visibility?: 'PUBLIC' | 'FOLLOWERS'
  createdAt: string
  updatedAt?: string
}

/** 标签 */
export interface TagInfo {
  id: number
  name: string
  postCount: number
}

/** 分类 */
export interface CategoryInfo {
  id: number
  name: string
  description?: string
  sort: number
}

/** 评论 */
export interface CommentInfo {
  id: number
  content: string
  postId: number
  userId: number
  username: string
  nickname: string
  avatar?: string
  parentId?: number
  likeCount: number
  createdAt: string
  children?: CommentInfo[]
}

/** 点赞请求 */
export interface LikeRequest {
  targetId: number
  targetType: 'POST' | 'COMMENT'
}

/** 通知信息 */
export interface NotificationInfo {
  id: number
  userId: number
  type: string
  content: string
  targetId?: number
  isRead: boolean
  createdAt: string
}

/** 关注状态 */
export interface FollowStatus {
  following: boolean
  followingCount: number
  followerCount: number
}

/** 收藏状态 */
export interface FavoriteStatus {
  favorited: boolean
}

/** 收藏夹 */
export interface FavoriteFolderInfo {
  id: number
  userId: number
  name: string
  sort: number
  createdAt: string
  updatedAt?: string
}

/** 私信会话 */
export interface ChatSessionInfo {
  userId: number
  username: string
  nickname?: string
  avatar?: string
  lastMessage: string
  unreadCount: number
  lastMessageAt: string
}

/** 私信消息 */
export interface ChatMessageInfo {
  id: number
  senderId: number
  receiverId: number
  senderName: string
  receiverName: string
  senderAvatar?: string
  receiverAvatar?: string
  content: string
  isRead: boolean
  createdAt: string
}

/** 积分信息 */
export interface UserPointInfo {
  userId: number
  points: number
  level: number
  signInDays: number
  signedInToday: boolean
  lastSignInAt?: string
}

/** 举报 */
export interface ReportInfo {
  id: number
  reporterId: number
  reporterName?: string
  targetType: 'POST' | 'COMMENT' | 'USER'
  targetId: number
  reason: string
  status: number
  handledBy?: number
  handleRemark?: string
  handledAt?: string
  createdAt: string
}

/** 敏感词 */
export interface SensitiveWordInfo {
  id: number
  word: string
  enabled: number
  createdAt: string
}

/** 审计日志 */
export interface AuditLogInfo {
  id: number
  operatorId?: number
  operatorName?: string
  action: string
  targetType?: string
  targetId?: number
  detail?: string
  createdAt: string
}
