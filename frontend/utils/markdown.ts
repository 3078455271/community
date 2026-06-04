const escapeHtml = (value: string) => value
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const renderInline = (value: string) => escapeHtml(value)
  .replace(/!\[([^\]]*)\]\((https?:\/\/[^)\s]+|\/[^)\s]+)\)/g, '<img src="$2" alt="$1" loading="lazy" />')
  .replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  .replace(/`([^`]+)`/g, '<code>$1</code>')
  .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  .replace(/\*([^*]+)\*/g, '<em>$1</em>')

export const renderMarkdown = (source = '') => {
  const blocks: string[] = []
  const protectedSource = source.replace(/```(\w*)\n?([\s\S]*?)```/g, (_match, lang: string, code: string) => {
    const token = `@@CODE_BLOCK_${blocks.length}@@`
    const languageClass = lang ? ` class="language-${escapeHtml(lang)}"` : ''
    blocks.push(`<pre><code${languageClass}>${escapeHtml(code.trim())}</code></pre>`)
    return token
  })

  const html = protectedSource
    .split(/\n{2,}/)
    .map((block) => {
      const trimmed = block.trim()
      if (!trimmed) return ''
      const protectedBlock = blocks.find((_item, index) => trimmed === `@@CODE_BLOCK_${index}@@`)
      if (protectedBlock) return protectedBlock
      if (trimmed.startsWith('### ')) return `<h3>${renderInline(trimmed.slice(4))}</h3>`
      if (trimmed.startsWith('## ')) return `<h2>${renderInline(trimmed.slice(3))}</h2>`
      if (trimmed.startsWith('# ')) return `<h1>${renderInline(trimmed.slice(2))}</h1>`
      if (trimmed.startsWith('> ')) return `<blockquote>${renderInline(trimmed.slice(2))}</blockquote>`
      if (/^- /.test(trimmed)) {
        const items = trimmed.split('\n').map(item => `<li>${renderInline(item.replace(/^- /, ''))}</li>`).join('')
        return `<ul>${items}</ul>`
      }
      return `<p>${renderInline(trimmed).replace(/\n/g, '<br>')}</p>`
    })
    .join('')

  return blocks.reduce((result, block, index) => result.replace(`@@CODE_BLOCK_${index}@@`, block), html)
}
