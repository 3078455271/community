/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
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
  viewCount: number
  likeCount: number
  commentCount: number
  createdAt: string
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