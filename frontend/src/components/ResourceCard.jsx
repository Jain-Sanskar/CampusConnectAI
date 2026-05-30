export default function ResourceCard({ resource, actions }) {
  return (
    <div className="bg-white rounded-lg border border-slate-200 p-5 flex flex-col hover:shadow-md transition">
      <div className="flex flex-wrap items-center gap-2 mb-2">
        {resource.category && (
          <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-brand-light text-brand-dark">
            {resource.category}
          </span>
        )}
        {resource.type && (
          <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">
            {resource.type}
          </span>
        )}
      </div>

      <h3 className="font-semibold text-slate-800">{resource.title}</h3>
      {resource.subject && (
        <p className="text-xs text-slate-400 mt-0.5">{resource.subject}</p>
      )}

      {resource.description && (
        <p className="text-sm text-slate-500 mt-2 flex-1">{resource.description}</p>
      )}

      <div className="mt-4 flex items-center justify-between gap-2">
        <a
          href={resource.resourceUrl}
          target="_blank"
          rel="noreferrer"
          className="text-sm font-medium text-brand hover:underline"
        >
          Open resource →
        </a>
        {actions}
      </div>
    </div>
  )
}
