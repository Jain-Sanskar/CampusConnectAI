import { useEffect, useRef, useState } from 'react'
import Navbar from '../components/Navbar'
import { sendMessage } from '../api/chatApi'

export default function Chat() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sessionId, setSessionId] = useState(null)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')

  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, sending])

  const handleSend = async (event) => {
    event.preventDefault()
    const text = input.trim()
    if (!text || sending) return

    setError('')
    setMessages((prev) => [...prev, { sender: 'user', text }])
    setInput('')
    setSending(true)

    try {
      const data = await sendMessage({ message: text, sessionId })
      setSessionId(data.sessionId)
      setMessages((prev) => [...prev, { sender: 'ai', text: data.reply }])
    } catch {
      setError('Could not reach the AI Senior. Please try again.')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar />
      <main className="flex-1 max-w-3xl w-full mx-auto px-4 py-6 flex flex-col">
        <div className="mb-4">
          <h1 className="text-2xl font-bold text-slate-800">AI Senior</h1>
          <p className="text-sm text-slate-500">
            Your friendly senior for academic and career guidance.
          </p>
        </div>

        <div className="flex-1 bg-white rounded-xl border border-slate-200 p-4 overflow-y-auto space-y-4 min-h-[50vh]">
          {messages.length === 0 && !sending && (
            <div className="h-full flex items-center justify-center text-center text-slate-400 py-12">
              <p>
                Hey! I&apos;m your AI Senior 🎓
                <br />
                Ask me about subjects, projects, placements or anything college.
              </p>
            </div>
          )}

          {messages.map((msg, index) => (
            <div
              key={index}
              className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[80%] rounded-2xl px-4 py-2 text-sm whitespace-pre-wrap ${
                  msg.sender === 'user'
                    ? 'bg-brand text-white rounded-br-sm'
                    : 'bg-slate-100 text-slate-800 rounded-bl-sm'
                }`}
              >
                {msg.text}
              </div>
            </div>
          ))}

          {sending && (
            <div className="flex justify-start">
              <div className="bg-slate-100 text-slate-500 rounded-2xl rounded-bl-sm px-4 py-2 text-sm">
                AI Senior is typing…
              </div>
            </div>
          )}

          <div ref={bottomRef} />
        </div>

        {error && (
          <div className="mt-3 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        <form onSubmit={handleSend} className="mt-4 flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask your AI Senior anything…"
            className="flex-1 rounded-full border border-slate-300 px-4 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
          />
          <button
            type="submit"
            disabled={sending || !input.trim()}
            className="rounded-full bg-brand px-5 py-2 text-sm font-semibold text-white hover:bg-brand-dark disabled:opacity-60 transition"
          >
            Send
          </button>
        </form>
      </main>
    </div>
  )
}
